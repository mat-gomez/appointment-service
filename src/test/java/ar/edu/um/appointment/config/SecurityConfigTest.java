package ar.edu.um.appointment.config;

import ar.edu.um.appointment.auth.TokenProvider;
import ar.edu.um.appointment.auth.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "jwt.secret=defaultSecretKeyForTestingPurposesWithAtLeast512BitsLengthHereToSatisfyJJWTRequirementsForHS512Algorithm"
})
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Test
    void securityBeans_ShouldBeLoadedSuccessfully() {
        assertNotNull(passwordEncoder);
        assertNotNull(securityFilterChain);
        assertNotNull(jwtDecoder);
        assertNotNull(tokenProvider);

        String rawPassword = "securePassword123";
        String encoded = passwordEncoder.encode(rawPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }

    @Test
    void jwtDecoder_ShouldSuccessfullyDecodeTokenCreatedByTokenProvider() {
        UserEntity user = UserEntity.builder()
                .username("melina")
                .authorities(Set.of("ROLE_USER"))
                .build();

        String token = tokenProvider.createToken(user);
        assertNotNull(token);

        Jwt decodedJwt = jwtDecoder.decode(token);
        assertNotNull(decodedJwt);
        assertEquals("melina", decodedJwt.getSubject());
    }
}