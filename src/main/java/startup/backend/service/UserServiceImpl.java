package startup.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import startup.backend.dto.UserDto;
import startup.backend.entity.User;
import startup.backend.enums.Status;
import startup.backend.exception.CustomException;
import startup.backend.repository.UserRepository;
import startup.backend.util.JwtTokenUtil;
import startup.backend.util.MessageConstant;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import startup.backend.entity.Role;
import jakarta.transaction.Transactional;
import startup.backend.repository.RefreshTokenRepository;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Slf4j
 public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtUtil;
    private final EmailServiceImpl emailService;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(MessageConstant.USER_NOT_FOUND_WITH_ID + id));
        return convertToDto(user);
    }

    public UserDto getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        System.out.println(username);
        User u= userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println(u);
        return convertToDto(u);
    }

    @Transactional
    @Override
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new CustomException(MessageConstant.USER_NOT_FOUND_WITH_ID + id);
        }
        refreshTokenRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        log.info("User deleted  successfully with ID: {}", id);
    }
    @Override
    public UserCounts getUserCounts() {
        List<User> users = userRepository.findAll();
        long studentCount = 0;
        long adminCount = 0;



        for (User user : users) {
            for (Role role : user.getRoles()) {
                switch (role.getName()) {
                    case ROLE_USER:
                        studentCount++;
                        break;
                    case ROLE_ADMIN:
                        adminCount++;
                        break;

                    default:
                        break;
                }
            }
        }


        return new UserCounts(studentCount, adminCount);
    }


    public byte[] getProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));
        return user.getProfileImage();
    }

    public record UserCounts(long students, long admins) {
    }

    @Override
    public void requestPasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = jwtUtil.generateToken(user.getUsername(),user.getUserId(), user.getRoles());
            String resetLink = "http://localhost:4200/reset-password?token=" + token;
            emailService.sendSimpleMessage(email, MessageConstant.PASSWORD_RESET_REQ, MessageConstant.CLICK_THE_LINK_TO_RESET_PASSWORD + resetLink);
            log.info("Password reset link sent to {}", email);
        } else {
            throw new CustomException(MessageConstant.EMAIL_ADDRESS_NOT_FOUND);
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        Claims claims = jwtUtil.extractResetPasswordClaims(token);
        if (Objects.isNull(claims)) {
            throw new CustomException(MessageConstant.INVALID_TOKEN);
        }

        String username = claims.getSubject();
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        if (!roles.contains("ROLE_ADMIN") && !roles.contains("ROLE_USER")) {
            throw new CustomException(MessageConstant.USER_DOES_NOT_HAVE_THE_ROLE);
        }
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Password reset successfully for user: {}", username);
        } else {
            throw new CustomException(MessageConstant.INVALID_TOKEN_OR_USER_NOT_FOUND);
        }
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setMobile_no(user.getMobileNo());
        userDto.setLocation(user.getLocation());
        userDto.setBio(user.getBio());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setStatus(user.getStatus());
        userDto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        userDto.setProfileImage(user.getProfileImage() != null ? encodeImageToBase64(user.getProfileImage()) : null);
        return userDto;
    }
        private String encodeImageToBase64(byte[] imageData) {
            return Base64.getEncoder().encodeToString(imageData);
    }
    public void uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new CustomException("File is empty. Please select a valid image.");
        }


        String contentType = file.getContentType();
        if (Objects.isNull(file.getContentType()) || !file.getContentType().startsWith("image")) {
            throw new CustomException("Invalid file type. Please upload an image.");
        }

        if (Objects.isNull(contentType) ||
                !Objects.equals(contentType, "image/jpeg") &&
                        !Objects.equals(contentType, "image/png") &&
                        !Objects.equals(contentType, "image/gif")) {
            throw new CustomException("Invalid image format. Please upload a JPEG, PNG, or GIF image.");
        }


        long maxFileSize = 5 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            throw new CustomException("File is too large. Maximum size is 5MB.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        user.setProfileImage(file.getBytes());
        userRepository.save(user);

        log.info("Profile image uploaded successfully for user with ID: {}", userId);
    }

    public UserDto updateUserStatus(String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Convert string to Status enum
        try {
            String formattedStatus = status.replace(' ', '_').toUpperCase();
            user.setStatus(Status.valueOf(formattedStatus));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
        }

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }
}