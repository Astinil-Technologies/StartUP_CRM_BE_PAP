package edutech.backend.security;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

        String base64EncodedKey = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("Base64-encoded JWT Secret Key: " + base64EncodedKey);
    }
}
