package ar.edu.um.appointment.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void extractUsername_ShouldReturnSubject_WhenJwtIsValid() {
        Jwt jwt = Jwt.withTokenValue("token-val")
                .header("alg", "none")
                .subject("usuario_test")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        String username = jwtService.extractUsername(jwt);

        assertEquals("usuario_test", username);
    }

    @Test
    void extractAuthorities_ShouldReturnRolesList_WhenRolesClaimExists() {
        Map<String, Object> claims = Map.of(
                "sub", "usuario_test",
                "roles", List.of("ROLE_USER", "ROLE_ADMIN")
        );

        Jwt jwt = Jwt.withTokenValue("token-val")
                .header("alg", "none")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        List<String> authorities = jwtService.extractAuthorities(jwt);

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
    }

    @Test
    void extractUsername_ShouldReturnNull_WhenJwtIsNull() {
        assertNull(jwtService.extractUsername(null));
    }

    @Test
    void extractAuthorities_ShouldReturnEmptyList_WhenJwtIsNull() {
        assertTrue(jwtService.extractAuthorities(null).isEmpty());
    }
}