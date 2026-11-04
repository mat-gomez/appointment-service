package ar.edu.um.appointment.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    @Value("${jwt.secret:defaultSecretKeyForTestingPurposesWithAtLeast256BitsLengthHere}")
    private String jwtSecret;

    @Value("${jwt.expiration:31536000000}")
    private long jwtExpiration;

    @SuppressWarnings("java:S5738")
    public String createToken(UserEntity user) {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(jwtExpiration);

        var authorities = user.getAuthorities().stream()
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", authorities)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }
}