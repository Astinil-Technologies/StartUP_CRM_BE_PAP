package startup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import startup.backend.entity.User;
import startup.backend.enums.Status;


import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String mobileNo;
    private Set<String> roles;
    private String profileImage;
    private String bio;
    private String location;
    private LocalDateTime createdAt;
    private Status status;

}

