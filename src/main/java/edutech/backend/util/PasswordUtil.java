package edutech.backend.util;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException(MessageConstant.PASSWORD_CANNOT_BE_NULL_OR_EMPTY);
        }
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            throw new IllegalArgumentException(MessageConstant.RAW_PASSWORD_AND_ENCODED_PASSWORD_CANNOT_BE_NULL);
        }
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }

    public boolean isPasswordStrong(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }
}
