package ar.edu.um.appointment.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
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
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        UserEntity user = UserEntity.builder()
                .username("melina")
                .email("melina@um.edu.ar")
                .password("hash123")
                .activated(true)
                .authorities(Set.of("ROLE_USER"))
                .build();

        userRepository.save(user);

        Optional<UserEntity> found = userRepository.findByUsername("melina");
        assertTrue(found.isPresent());
        assertEquals("melina@um.edu.ar", found.get().getEmail());
        assertTrue(userRepository.existsByEmail("melina@um.edu.ar"));
    }
}