package edutech.backend.service;

import edutech.backend.dto.ApiResponse;
import edutech.backend.dto.LoginRequest;
import edutech.backend.dto.SignupRequest;
import java.util.Map;

public interface AuthService {
    ApiResponse<Map<String, String>> registerUser(SignupRequest signupRequest);
    ApiResponse<Map<String, String>> authenticateUser(LoginRequest loginRequest);
    ApiResponse<Map<String, String>> refreshAccessToken(String refreshToken);
}
