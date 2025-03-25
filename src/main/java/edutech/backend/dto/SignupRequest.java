package edutech.backend.dto;
import lombok.Builder;
import lombok.Data;
import java.util.Set;
import java.util.HashSet;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

@Data
@Builder
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
    private Set<String> role;

    public Set<String> getRole() {
        return (role != null) ? role : new HashSet<>();
    }
}
