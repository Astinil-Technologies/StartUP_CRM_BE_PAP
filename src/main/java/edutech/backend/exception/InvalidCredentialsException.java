package edutech.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends CustomException {

    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
}