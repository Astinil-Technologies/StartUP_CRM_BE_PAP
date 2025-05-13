package startup.backend.entity;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import startup.backend.dto.UserDto;
import startup.backend.util.Utility;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = false)
    private String firstName;

    @Column(nullable = false, unique = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name="mobile_no",nullable = false, unique = true)
    private String mobileNo;
    @Lob
    @Column(name = "profile_image", columnDefinition = "LONGBLOB")
    private byte[] profileImage;

    private String bio;

    private String location;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    public Integer getUserId() {
        return id.intValue();
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public UserDto mapUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setMobileNo(user.getMobileNo());
        dto.setLocation(user.getLocation());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setProfileImage(user.getProfileImage() != null ? Utility.encodeImageToBase64(user.getProfileImage()) : null);
        return dto;
    }

}


