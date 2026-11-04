package ar.edu.um.appointment.auth;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AccountDTOTest {

    @Test
    void testAccountDTOBuilderAndGetters() {
        AccountDTO dto = AccountDTO.builder()
                .login("melina")
                .email("mat.gomez@alumno.um.edu.ar")
                .firstName("Melina")
                .lastName("Gomez Torres")
                .activated(true)
                .authorities(Set.of("ROLE_USER"))
                .build();

        assertEquals("melina", dto.getLogin());
        assertEquals("mat.gomez@alumno.um.edu.ar", dto.getEmail());
        assertTrue(dto.isActivated());
        assertTrue(dto.getAuthorities().contains("ROLE_USER"));
    }
}