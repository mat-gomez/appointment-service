package ar.edu.um.appointment.auth;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void testUserEntityBuilderAndGetters() {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .username("melina")
                .email("melina@um.edu.ar")
                .password("securepass")
                .activated(true)
                .authorities(Set.of("ROLE_ADMIN"))
                .build();

        assertEquals(1L, user.getId());
        assertEquals("melina", user.getUsername());
        assertEquals("melina@um.edu.ar", user.getEmail());
        assertEquals("securepass", user.getPassword());
        assertTrue(user.isActivated());
        assertTrue(user.getAuthorities().contains("ROLE_ADMIN"));
    }
}