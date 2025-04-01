package startup.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import io.jsonwebtoken.Jwts;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.LoginRequest;
import startup.backend.dto.SignupRequest;
import startup.backend.entity.*;
import startup.backend.exception.CustomException;
import startup.backend.repository.RoleRepository;
import startup.backend.repository.UserRepository;
import startup.backend.util.JwtTokenUtil;
import startup.backend.util.MessageConstant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static ch.qos.logback.core.util.StringUtil.isNullOrEmpty;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenServiceImpl refreshTokenServiceImpl;

    @Override
    public ApiResponse<Map<String, String>> registerUser(SignupRequest signupRequest) {
        log.info("Initiating user registration process for username: {}", signupRequest.getUsername());

        // Check if the user already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            log.warn("Registration failed due to duplicate email: {}", signupRequest.getEmail());
            throw new CustomException(MessageConstant.EMAIL_IS_ALREADY_IN_USE);
        }
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            log.warn("Registration failed due to duplicate username: {}", signupRequest.getUsername());
            throw new CustomException(MessageConstant.USERNAME_ALREADY_IN_USE);
        }
        if (userRepository.existsByMobileNo(signupRequest.getMobile_no())) {
            log.warn("Registration failed due to duplicate mobile number: {}", signupRequest.getMobile_no());
            throw new CustomException(MessageConstant.MOBILENO_IS_ALREADY_IN_USE);
        }

        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setMobileNo(signupRequest.getMobile_no());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setLocation(signupRequest.getLocation());
        user.setBio(signupRequest.getBio());
        user.setCreatedAt(signupRequest.getCreatedAt());

        Set<Role> userRoles = resolveRoles(signupRequest);
        user.setRoles(userRoles);
        log.debug("Assigning roles to the user: {}", userRoles);

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        Map<String, String> tokens = generateTokens(user);

        return ApiResponse.successWithTokens(MessageConstant.USER_REGISTERED_SUCCESSFULLY, tokens, HttpStatus.CREATED.value());
    }

    private Set<Role> resolveRoles(SignupRequest signupRequest) {
        log.debug("Resolving roles for new user with username: {}", signupRequest.getUsername());

        Set<Role> roles = Optional.ofNullable(signupRequest.getRole())
                .orElse(Collections.emptySet())
                .stream()
                .map(roleName -> {
                    RoleName roleEnum;
                    try {
                        roleEnum = RoleName.valueOf(roleName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid role name provided for user '{}': {}", signupRequest.getUsername(), roleName, e);
                        throw new CustomException(MessageConstant.INVALID_ROLE + ": " + roleName);
                    }
                    return roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new CustomException("Role not found: " + roleName));
                })
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            log.warn("No roles provided in the signup request. Assigning default user role.");
            roles = Set.of(
                    roleRepository.findByName(RoleName.ROLE_USER)
                            .orElseThrow(() -> new CustomException(MessageConstant.DEFAULT_ROLE_NOT_FOUND))
            );
        }

        return roles;
    }

    @Override
    public ApiResponse<Map<String, String>> authenticateUser(LoginRequest loginRequest) {
        String identifier = Optional.ofNullable(loginRequest.getUsername())
                .orElse(Optional.ofNullable(loginRequest.getEmail())
                        .orElse(loginRequest.getMobile_no()));

        log.debug("Authenticating user with identifier: {}", identifier);
        if (isNullOrEmpty(identifier)) {
            throw new CustomException(MessageConstant.INVALID_USERNAME_OR_PASSWORD, HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, loginRequest.getPassword())
            );

            log.info("User authenticated successfully: {}", authentication.getName());

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new CustomException(MessageConstant.INVALID_USERNAME_OR_PASSWORD, HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

            Map<String, String> tokens = generateTokens(user);

            tokens.put("role", user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .findFirst().orElse("ROLE_USER"));

            return ApiResponse.successWithTokens("Authentication successful", tokens, HttpStatus.OK.value());
        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for identifier: {}", identifier);
            throw new CustomException(MessageConstant.INVALID_USERNAME_EMAIL_MOBILE, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        } catch (Exception ex) {
            log.error("An unexpected error occurred during authentication for identifier: {}", identifier, ex);
            throw new CustomException(MessageConstant.AUTHENTICATION_FAILED_DUE_TO_SERVER_ERROR_TRY_AGAIN_LATER, HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_ERROR");
        }
    }

    @Override
    public ApiResponse<Map<String, String>> refreshAccessToken(String refreshToken) {
        log.info("Refreshing access token using refresh token: {}", refreshToken);

        if (Objects.isNull(refreshToken) || refreshToken.trim().isEmpty()) {
            throw new CustomException(MessageConstant.REFRESH_TOKEN_CANNOT_BE_NULL_OR_EMPTY);
        }

        RefreshToken token = refreshTokenServiceImpl.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(MessageConstant.REFRESH_TOKEN_INVALID));

        if (refreshTokenServiceImpl.isRevoked(token)) {
            throw new CustomException(MessageConstant.REFRESH_TOKEN_IS_REVOKED_PLEASE_LOGIN_AGAIN);
        }

        if (refreshTokenServiceImpl.isExpired(token)) {
            throw new CustomException(MessageConstant.REFRESH_TOKEN_IS_EXPIRED_PLEASE_LOGIN_AGAIN);
        }

        refreshTokenServiceImpl.revokeToken(refreshToken);
        User user = token.getUser();
        Map<String, String> tokens = generateTokens(user);

        return ApiResponse.successWithTokens(MessageConstant.ACCESS_TOKEN_REFRESHED_SUCESSFULLY, tokens, HttpStatus.OK.value());
    }

    private Map<String, String> generateTokens(User user) {
        // Generate the tokens
        String accessToken = jwtTokenUtil.generateToken(user.getUsername(), user.getUserId(), user.getRoles());
        String refreshToken = refreshTokenServiceImpl.createRefreshToken(user).getToken();

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    @Transactional
    public ApiResponse<Map<String, String>> authenticateWithGoogle(String idToken) {
        GoogleUser googleUser = verifyGoogleToken(idToken);
        User user = userRepository.findByEmail(googleUser.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(googleUser.getEmail());
                    newUser.setEmail(googleUser.getEmail());
                    newUser.setFirstName(googleUser.getFirstName());
                    newUser.setLastName(googleUser.getLastName());

                    try {
                        byte[] profileImageBytes = convertImageUrlToBytes(googleUser.getPicture());
                        newUser.setProfileImage(profileImageBytes);
                    } catch (IOException e) {
                        log.error("Error converting image URL to byte array", e);
                    }

                    newUser.setPassword(UUID.randomUUID().toString());

                    Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                            .orElseThrow(() -> new CustomException("Role ROLE_USER not found"));

                    // Set the user's roles
                    newUser.setRoles(Set.of(userRole)); // Setting roles as a Set
                    return userRepository.save(newUser);
                });

        Map<String, String> tokens = generateTokens(user);

        return ApiResponse.successWithTokens("Authentication Successful", tokens, 200);
    }



    public GoogleUser verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId)) // Replace with actual Client ID
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new CustomException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            System.out.println("Google Token Payload: " + payload); // Debugging line

            String email = payload.getEmail();
            String fullName = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            String[] nameParts = fullName.split(" ", 2);
            String firstName = nameParts.length > 0 ? nameParts[0] : "";
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            return new GoogleUser(email, firstName, lastName, picture);
        } catch (Exception e) {
            throw new CustomException("Failed to verify Google token: " + e.getMessage());
        }
    }

    public byte[] convertImageUrlToBytes(String imageUrl) throws IOException {
        // Create a URL object from the image URL string
        URL url = new URL(imageUrl);

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Open an InputStream to read the image
        try (InputStream in = connection.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            // Read the image data in chunks and write it to the ByteArrayOutputStream
            while ((bytesRead = in.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            // Return the byte array of the image
            return byteArrayOutputStream.toByteArray();
        }
    }
}