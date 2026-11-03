package ar.edu.um.appointment.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityConfigTest {

    @Test
    void securityConfig_ShouldBeInstantiated() {
        SecurityConfig securityConfig = new SecurityConfig();
        assertNotNull(securityConfig);
    }
}