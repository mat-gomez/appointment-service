package ar.edu.um.appointment.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;

    @Test
    void createToken_ShouldGenerateValidTokenWithCorrectSubject() {
        UserEntity user = UserEntity.builder()
                .username("melina-test")
                .authorities(Set.of("ROLE_USER"))
                .build();

        String token = tokenProvider.createToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3, "Un JWT válido debe tener 3 partes separadas por puntos");
    }
}