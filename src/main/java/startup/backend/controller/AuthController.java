package startup.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.LoginRequest;
import startup.backend.dto.SignupRequest;
import startup.backend.exception.CustomException;
import startup.backend.exception.InvalidCredentialsException;
import startup.backend.service.AuthServiceImpl;
import startup.backend.util.MessageConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
//@CrossOrigin("http://localhost:4200")
@CrossOrigin("http://13.232.97.18:4200")
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/register")

    public ResponseEntity<ApiResponse<Map<String, String>>> registerUser(
            @RequestPart("data") String signupDataJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            // Convert JSON string to SignupRequest object
            ObjectMapper objectMapper = new ObjectMapper();
            SignupRequest signupRequest = objectMapper.readValue(signupDataJson, SignupRequest.class);

            // Set the image
            signupRequest.setProfileImage(profileImage);

            ApiResponse<Map<String, String>> response = authServiceImpl.registerUser(signupRequest);
            return ResponseEntity.status(response.getCode()).body(response);
        } catch (Exception e) {
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
