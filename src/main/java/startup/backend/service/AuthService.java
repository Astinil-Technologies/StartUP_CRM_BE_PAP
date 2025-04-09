package startup.backend.service;

import startup.backend.dto.ApiResponse;
import startup.backend.dto.LoginRequest;
import startup.backend.dto.SignupRequest;
import java.util.Map;

public interface AuthService {
    ApiResponse<Map<String, String>> registerUser(SignupRequest signupRequest);
    ApiResponse<Map<String, String>> authenticateUser(LoginRequest loginRequest);
    ApiResponse<Map<String, String>> refreshAccessToken(String refreshToken);


}
