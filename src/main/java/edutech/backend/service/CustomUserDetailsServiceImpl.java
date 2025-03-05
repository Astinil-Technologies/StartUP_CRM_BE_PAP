package edutech.backend.service;

import edutech.backend.entity.User;
import edutech.backend.repository.UserRepository;
import edutech.backend.security.CustomUserDetails;
import edutech.backend.util.MessageConstant;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user's details based on their unique identifier, such as email, username, or mobile number.
     * Searches in the database using the repository methods and throws an exception if not found.
     *
     * @param identifier the identifier of the user (email, username, or mobile number)
     * @return UserDetails object encapsulating user's information
     * @throws UsernameNotFoundException if no user is found with the specified identifier
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .or(() -> userRepository.findByMobileNo(identifier))
                .orElseThrow(() -> new UsernameNotFoundException(
                        MessageConstant.USER_NOT_FOUND_WITH_IDENTIFIER + identifier
                ));

        return new CustomUserDetails(user); // Return the CustomUserDetails object
    }
}
