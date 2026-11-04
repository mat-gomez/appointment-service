package ar.edu.um.appointment.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidRequest_thenNoViolations() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setLogin("melina");
        dto.setPassword("password123");
        dto.setEmail("mat.gomez@alumno.um.edu.ar");
        dto.setFirstName("Melina");
        dto.setLastName("Gomez");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenBlankLogin_thenValidationFails() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setLogin("");
        dto.setPassword("password123");
        dto.setEmail("mat.gomez@alumno.um.edu.ar");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void whenInvalidEmail_thenValidationFails() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setLogin("melina");
        dto.setPassword("password123");
        dto.setEmail("not-an-email");

        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}