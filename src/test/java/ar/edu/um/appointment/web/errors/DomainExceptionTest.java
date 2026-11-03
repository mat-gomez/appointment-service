package ar.edu.um.appointment.web.errors;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionTest {

    @Test
    void domainException_ShouldStoreMessageStatusAndCodeCorrectly() {
        String message = "Error de negocio en el turno";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String code = "INVALID_APPOINTMENT_STATE";

        DomainException exception = new DomainException(message, status, code);

        assertEquals(message, exception.getMessage());
        assertEquals(status, exception.getStatus());
        assertEquals(code, exception.getCode());
    }
}