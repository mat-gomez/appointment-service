package ar.edu.um.appointment.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JWTTokenDTOTest {

    @Test
    void testJWTTokenDTOProperties() {
        JWTTokenDTO dto = new JWTTokenDTO("dummy-token-123");
        assertEquals("dummy-token-123", dto.getIdToken());

        dto.setIdToken("new-token-456");
        assertEquals("new-token-456", dto.getIdToken());
    }
}