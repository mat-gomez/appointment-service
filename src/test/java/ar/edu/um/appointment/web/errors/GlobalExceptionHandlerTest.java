package ar.edu.um.appointment.web.errors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleDomainException_ShouldReturnProblemDetailWithCorrectDetails() {
        DomainException ex = new DomainException("Turno no encontrado", HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND");

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleDomainException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Turno no encontrado", response.getBody().getDetail());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getProperties());
        assertEquals("APPOINTMENT_NOT_FOUND", response.getBody().getProperties().get("code"));
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerErrorProblemDetail() {
        Exception ex = new RuntimeException("Error inesperado en el servidor");

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleGenericException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error inesperado en el servidor", response.getBody().getDetail());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getProperties());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getProperties().get("code"));
    }
}