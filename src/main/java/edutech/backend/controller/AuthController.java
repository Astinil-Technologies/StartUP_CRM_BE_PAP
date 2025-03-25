package edutech.backend.controller;

import edutech.backend.dto.ApiResponse;
import edutech.backend.dto.LoginRequest;
import edutech.backend.dto.SignupRequest;
import edutech.backend.exception.CustomException;
import edutech.backend.exception.InvalidCredentialsException;
import edutech.backend.service.AuthServiceImpl;
import edutech.backend.util.MessageConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@CrossOrigin("http://localhost:4200")
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> registerUser(@RequestBody SignupRequest signupRequest) {
        try {
            ApiResponse<Map<String, String>> response = authServiceImpl.registerUser(signupRequest);
            return ResponseEntity.status(response.getCode()).body(response);
        } catch (Exception e) {
            // Use the updated error handling for the response
            ApiResponse<Map<String, String>> errorResponse = ApiResponse.error(
                    MessageConstant.REGISTRATION_FAILED + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            ApiResponse<Map<String, String>> response = authServiceImpl.authenticateUser(loginRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (InvalidCredentialsException e) {
            ApiResponse<Map<String, String>> errorResponse = ApiResponse.error(
                    e.getMessage(),
                    e.getStatus().value()
            );
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Map<String, String>> errorResponse = ApiResponse.error(
                    MessageConstant.AUTHENTICATION_FAILED + e.getMessage(),
                    HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshAccessToken(@RequestBody Map<String ,String>request) {
        try {
            String refreshToken = request.get("refreshToken");
            ApiResponse<Map<String, String>> response = authServiceImpl.refreshAccessToken(refreshToken);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<Map<String, String>> errorResponse = ApiResponse.error(
                    "Failed to refresh token: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    @PostMapping("/google")
    public ApiResponse<Map<String, String>> authenticateWithGoogle(@RequestBody Map<String, String> request) {
        String googleToken = request.get("googleToken");
        if (googleToken == null || googleToken.isEmpty()) {
            throw new CustomException("Google token is missing");
        }
        return authServiceImpl.authenticateWithGoogle(googleToken);
    }
}
