package you.v50to.eatwhat.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class JwtUtil {

    private JwtUtil() {}

    public record User(
            String casId,
            String name
    ) {}


    public static final String CLAIM_KEY_CAS_ID = "casId";
    public static final String CLAIM_KEY_NAME = "name";

    /**
     * 校验 JWT 并直接提取 User record
     */
    public static Optional<User> getClaim(String token, String secret) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey(secret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String casId = claims.get(CLAIM_KEY_CAS_ID, String.class);
            String name = claims.get(CLAIM_KEY_NAME, String.class);

            if (casId == null || name == null) {
                return Optional.empty();
            }

            return Optional.of(new User(casId, name));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static SecretKey secretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
