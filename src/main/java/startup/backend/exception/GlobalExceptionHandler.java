package startup.backend.exception;

import startup.backend.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Handle CustomException FIRST (most specific)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        logger.error("CustomException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 2. Handle JwtTokenExpiredException SECOND (more specific)
    @ExceptionHandler(JwtTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtTokenExpiredException(JwtTokenExpiredException ex) {
        logger.error("JWT Token Expired: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error("JWT Token has expired. Please re-authenticate.", HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // 3. Handle AccessDeniedException (Important!)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        logger.error("Access Denied: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error("Access Denied", HttpStatus.FORBIDDEN.value()); // Or a more specific message
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }


    // 4. Handle all other exceptions LAST (most general)
    @ExceptionHandler(Exception.class)  // Catch-all (least specific)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        logger.error("Unexpected error: ", ex); // Log the *full* exception (including stack trace)
        ApiResponse<Void> response = ApiResponse.error("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value()); // Generic message for client
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
