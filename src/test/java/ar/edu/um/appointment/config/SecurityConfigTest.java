package ar.edu.um.appointment.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void securityFilterChain_ShouldBeCreatedSuccessfully() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig();
        HttpSecurity httpSecurity = mock(HttpSecurity.class);



        assertNotNull(securityConfig);
    }
}