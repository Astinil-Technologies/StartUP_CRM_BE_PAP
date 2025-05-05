package startup.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @NotNull(message = "FirstName is required")
    @NotEmpty(message = "FirstName cannot be empty")
    private String firstName;
    @NotNull(message = "LastName is required")
    @NotEmpty(message = "LastName cannot be empty")
    private String lastName;
    @NotNull(message = "Username is required")
    @NotEmpty(message = "Username cannot be empty")
    private String username;
    @NotNull(message = "Email is required")
    @NotEmpty(message = "Email cannot be empty")
    private String email;
    @NotNull(message = "Password is required")
    @NotEmpty(message = "Password cannot be empty")
    private String password;
    @NotNull(message = "Mobile number is required")
    @NotEmpty(message = "Mobile number cannot be empty")
    private String mobile_no;
    private String bio;
    private String location;
    private LocalDateTime createdAt;
    private Set<String> role;

    // ✅ Add profile image field
    @JsonIgnore
    private MultipartFile profileImage;


    public Set<String> getRole() {
        return (role != null) ? role : new HashSet<>();
    }
}
