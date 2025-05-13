package startup.backend.controller;

import startup.backend.dto.ApiResponse;
import startup.backend.dto.ForgotPasswordRequest;
import startup.backend.dto.ForgotPasswordReset;
import startup.backend.dto.UserDto;
import startup.backend.entity.User;
import startup.backend.service.UserServiceImpl;
import startup.backend.util.MessageConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin("http://localhost:4200")
public class UserController {

    private final UserServiceImpl userServiceImpl;

    @PostMapping("/request-password-reset")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@RequestBody ForgotPasswordRequest request) {
        try {
            userServiceImpl.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(MessageConstant.PASSWORD_RESET_LINK_SENT, null, HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error sending password reset link: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ForgotPasswordReset resetDto) {
        try {
            userServiceImpl.resetPassword(resetDto.getToken(), resetDto.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success(MessageConstant.PASSWORD_RESET_SUCESSFULLY, null, HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error resetting password: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        try {
            List<UserDto> users = userServiceImpl.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success(MessageConstant.USERS_RETRIEVED_SUCCESSFULLY, users, HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving users: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_INSTRUCTOR')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        try {
            UserDto userDto = userServiceImpl.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(MessageConstant.USER_RETRIEVED_SUCCESSFULLY, userDto, HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUserById(@PathVariable Long id) {
        try {
            userServiceImpl.deleteUserById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(MessageConstant.USER_DELETED_SUCCESSFULLY, null, HttpStatus.NO_CONTENT.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile(){

        System.out.println("Profile is called");
        UserDto u = userServiceImpl.getUserProfile();
        return new  ResponseEntity<UserDto>(u,HttpStatus.OK);
    }

    @GetMapping("/user-counts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserServiceImpl.UserCounts>> getUserCounts() {
        try {
            UserServiceImpl.UserCounts userCounts = userServiceImpl.getUserCounts();
            return ResponseEntity.ok(ApiResponse.success("User counts fetched successfully", userCounts, HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching user counts: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/upload-image/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> uploadProfileImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            userServiceImpl.uploadProfileImage(id, file);
            return ResponseEntity.ok(ApiResponse.success("Profile image uploaded successfully", null, HttpStatus.OK.value()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error uploading profile image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/update-profile")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto){
        userDto = userServiceImpl.updateUser(userDto);
        return  new ResponseEntity<UserDto>(userDto,HttpStatus.OK);
    }
}