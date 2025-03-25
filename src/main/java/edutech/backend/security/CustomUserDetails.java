package edutech.backend.security;

import edutech.backend.entity.Role;
import edutech.backend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    @Getter
    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * -- GETTER --
     *  Retrieves roles associated with the user.
     *  This method is added to fix the issue mentioned in `AuthServiceImpl`.
     */

    @Getter
    private final Set<Role> roles;

    /**
     * Constructor that initializes the UserDetails object with user data.
     */
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.roles = user.getRoles();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the authorities (Spring Security roles) granted to the user.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Retrieves the password of the user.
     */
    @Override
    public String getPassword()
    { return password; }

    /**
     * Retrieves the username of the user.
     */
    @Override
    public String getUsername()
    { return username;}

    /**
     * Indicates if the user's account is expired.
     */
    @Override
    public boolean isAccountNonExpired()
    {return true;}

    /**
     * Indicates if the user's account is locked.
     */
    @Override
    public boolean isAccountNonLocked()
    { return true; }

    /**
     * Indicates if the user's credentials are expired.
     */
    @Override
    public boolean isCredentialsNonExpired()
    { return true; }

    /**
     * Indicates whether this user is enabled.
     */
    @Override
    public boolean isEnabled()
    { return true; }
}