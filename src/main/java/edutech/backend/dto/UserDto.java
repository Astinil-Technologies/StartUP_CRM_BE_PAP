package edutech.backend.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String mobile_no;
    private Set<String> roles;
    private String profileImage;
}

