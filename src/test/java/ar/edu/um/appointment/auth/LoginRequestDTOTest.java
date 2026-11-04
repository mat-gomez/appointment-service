package ar.edu.um.appointment.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidLogin_thenNoViolations() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("melina");
        dto.setPassword("password123");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenBlankUsername_thenValidationFails() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("");
        dto.setPassword("password123");

        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}