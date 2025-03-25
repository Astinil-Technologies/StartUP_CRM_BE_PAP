package edutech.backend.service;

import edutech.backend.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface UserService {
    UserDto getUserById(Long id);
    void deleteUserById(Long id);
    UserServiceImpl.UserCounts getUserCounts();
    byte[] getProfileImage(Long userId);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void uploadProfileImage(Long userId, MultipartFile file) throws IOException;
    List<UserDto> getAllUsers();
}
