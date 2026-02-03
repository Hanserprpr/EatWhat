package you.v50to.eatwhat.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class JwtUtil {

    private JwtUtil() {}

    public record User(String casId, String name) {}

    private static final String CLAIM_KEY_CAS_ID = "casID";
    private static final String CLAIM_KEY_NAME = "name";

    private static final byte[] SALT =
            "KOISHIKISHIKAWAIIKAWAIIKISSKISSLOVELY".getBytes(StandardCharsets.UTF_8);
    private static final int ITERATION_COUNT = 114514;
    private static final int KEY_LENGTH_BITS = 256;

    public static Optional<User> getClaim(String token, String key) {
        if (token == null || key == null) return Optional.empty();
        token = token.trim();

        try {
            SecretKey secretKey = generateSecretKey(key);

            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String casId = claims.get(CLAIM_KEY_CAS_ID, String.class);
            String name = claims.get(CLAIM_KEY_NAME, String.class);

            if (casId == null || name == null) return Optional.empty();
            return Optional.of(new User(casId, name));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static SecretKey generateSecretKey(String key) {
        try {
            PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] secretBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(secretBytes, "HmacSHA256");
        } catch (Exception e) {
            throw new IllegalStateException("failed to derive jwt key", e);
        }
    }
}
