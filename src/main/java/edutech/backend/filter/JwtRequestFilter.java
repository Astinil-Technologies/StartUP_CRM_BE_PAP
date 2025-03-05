package edutech.backend.filter;

import edutech.backend.security.CustomUserDetails;
import edutech.backend.service.CustomUserDetailsServiceImpl;
import edutech.backend.util.JwtTokenUtil;
import edutech.backend.exception.JwtTokenExpiredException;
import edutech.backend.exception.JwtTokenException;
import edutech.backend.exception.JwtTokenParseException;
import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Objects;
@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsServiceImpl customUserDetailsService; // Change to CustomUserDetailsServiceImpl


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractTokenFromRequest(request);
        try {
            if (Objects.nonNull(token) && isValidJwtFormat(token)) {
                String username = jwtTokenUtil.extractUsername(token);

                if (request.getRequestURI().startsWith("/auth/")) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Use a primitive boolean expression here
                boolean isTokenValid = jwtTokenUtil.validateToken(token, username);
                boolean isNoAuthentication = Objects.isNull(SecurityContextHolder.getContext().getAuthentication());

                if (isTokenValid && isNoAuthentication) {
                    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);

                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication); // Authentication ONLY
                    }
                }
            }
        } catch (JwtTokenExpiredException ex) {
            String errorMessage = "JWT Token Expired: The token has expired. Expiration time: " + ex.getMessage();
            logger.error(errorMessage);
            sendErrorResponse(response, errorMessage, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (JwtTokenException | JwtTokenParseException ex) {
            logger.warn("JWT Token Error: " + ex.getMessage());
            sendErrorResponse(response, ex.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (Exception ex) {
            logger.warn("Failed to authenticate user with the token: " + token, ex);
            sendErrorResponse(response, "Authentication Failed", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }


    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean isValidJwtFormat(String token) {
        return token.chars().filter(ch -> ch == '.').count() == 2;
    }
}
