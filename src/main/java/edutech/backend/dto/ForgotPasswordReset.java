package edutech.backend.dto;

import lombok.Data;

@Data
public class ForgotPasswordReset {
    private String token;
    private String newPassword;
}