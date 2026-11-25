package ar.edu.um.appointment.web.errors;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDomainExceptionSanitizesCodeWithSpaces() {
        DomainException ex = new DomainException(
                "User already has an active pending reservation",
                HttpStatus.CONFLICT,
                "ACTIVE RESERVATION EXISTS"
        );

        ResponseEntity<ProblemDetail> response = handler.handleDomainException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType())
                .isEqualTo(URI.create("https://api.appointment.um.edu.ar/errors/active-reservation-exists"));
        assertThat(response.getBody().getDetail()).isEqualTo("User already has an active pending reservation");
    }

    @Test
    void handleDomainExceptionUsesFallbackWhenCodeIsNull() {
        DomainException ex = new DomainException("Invalid request", HttpStatus.BAD_REQUEST, null);

        ResponseEntity<ProblemDetail> response = handler.handleDomainException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType())
                .isEqualTo(URI.create("https://api.appointment.um.edu.ar/errors/domain-error"));
    }

    @Test
    void handleDomainExceptionUsesFallbackWhenCodeIsBlank() {
        DomainException ex = new DomainException("Blank code", HttpStatus.BAD_REQUEST, "   ");

        ResponseEntity<ProblemDetail> response = handler.handleDomainException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType())
                .isEqualTo(URI.create("https://api.appointment.um.edu.ar/errors/domain-error"));
    }

    @Test
    void handleGenericExceptionReturns500() {
        ResponseEntity<ProblemDetail> response = handler.handleGenericException(new RuntimeException("Boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getType())
                .isEqualTo(URI.create("https://api.appointment.um.edu.ar/errors/internal-error"));
    }
}
