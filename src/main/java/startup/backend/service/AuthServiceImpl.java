package startup.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import io.jsonwebtoken.Jwts;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.LoginRequest;
import startup.backend.dto.SignupRequest;
import startup.backend.dto.*;
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
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.*;
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

        // Duplicate checks
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new CustomException(MessageConstant.EMAIL_IS_ALREADY_IN_USE);
        }
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new CustomException(MessageConstant.USERNAME_ALREADY_IN_USE);
        }
        if (userRepository.existsByMobileNo(signupRequest.getMobile_no())) {
            throw new CustomException(MessageConstant.MOBILENO_IS_ALREADY_IN_USE);
        }

        // Create user
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

        // ✅ Set image
        try {
            if (signupRequest.getProfileImage() != null && !signupRequest.getProfileImage().isEmpty()) {
                user.setProfileImage(signupRequest.getProfileImage().getBytes());
            }
        } catch (IOException e) {
            throw new CustomException("Failed to process profile image.");
        }

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        Map<String, String> tokens = generateTokens(user);
        return ApiResponse.successWithTokens(MessageConstant.USER_REGISTERED_SUCCESSFULLY, tokens, HttpStatus.CREATED.value());
    }

    private Set<Role> resolveRoles(SignupRequest signupRequest) {
        log.debug("Resolving roles for user: {}", signupRequest.getUsername());

        Set<Role> roles = Optional.ofNullable(signupRequest.getRole())
                .orElse(Collections.emptySet())
                .stream()
                .map(roleName -> {
                    RoleName roleEnum;
                    try {
                        roleEnum = RoleName.valueOf(roleName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new CustomException(MessageConstant.INVALID_ROLE + ": " + roleName);
                    }
                    return roleRepository.findByName(roleEnum)
                            .orElseGet(() -> createRoleIfNotFound(roleEnum));
                })
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            log.warn("No roles provided. Assigning default ROLE_USER.");
            Role defaultRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseGet(() -> createRoleIfNotFound(RoleName.ROLE_USER));
            roles = Set.of(defaultRole);
        }

        return roles;
    }

    private Role createRoleIfNotFound(RoleName roleName) {
        log.info("Role '{}' not found. Creating default role.", roleName);
        Role newRole = new Role();
        newRole.setName(roleName);
        return roleRepository.save(newRole);
    }

    @Override
    public ApiResponse<Map<String, String>> authenticateUser(LoginRequest loginRequest) {
        String identifier = Optional.ofNullable(loginRequest.getUsername())
                .orElse(Optional.ofNullable(loginRequest.getEmail())
                        .orElse(loginRequest.getMobile_no()));

        if (isNullOrEmpty(identifier)) {
            throw new CustomException(MessageConstant.INVALID_USERNAME_OR_PASSWORD, HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, loginRequest.getPassword())
            );

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new CustomException(MessageConstant.INVALID_USERNAME_OR_PASSWORD, HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

            Map<String, String> tokens = generateTokens(user);
            tokens.put("role", user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .findFirst().orElse("ROLE_USER"));

            return ApiResponse.successWithTokens("Authentication successful", tokens, HttpStatus.OK.value());
        } catch (BadCredentialsException e) {
            throw new CustomException(MessageConstant.INVALID_USERNAME_EMAIL_MOBILE, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        } catch (Exception ex) {
            throw new CustomException(MessageConstant.AUTHENTICATION_FAILED_DUE_TO_SERVER_ERROR_TRY_AGAIN_LATER, HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_ERROR");
        }
    }

    @Override
    public ApiResponse<Map<String, String>> refreshAccessToken(String refreshToken) {
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
                            .orElseGet(() -> createRoleIfNotFound(RoleName.ROLE_USER));

                    newUser.setRoles(Set.of(userRole));
                    return userRepository.save(newUser);
                });

        Map<String, String> tokens = generateTokens(user);
        return ApiResponse.successWithTokens("Authentication Successful", tokens, 200);
    }



    public GoogleUser verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new CustomException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
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
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStream in = connection.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            return byteArrayOutputStream.toByteArray();
        }
    }
}
