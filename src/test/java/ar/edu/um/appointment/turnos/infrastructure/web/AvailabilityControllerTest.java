package ar.edu.um.appointment.turnos.infrastructure.web;

import ar.edu.um.appointment.turnos.application.ports.in.GetAvailabilityUseCase;
import ar.edu.um.appointment.turnos.domain.model.AvailabilityResponseDTO;
import ar.edu.um.appointment.turnos.domain.model.AvailabilitySlotDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityControllerTest {

    @Mock
    private GetAvailabilityUseCase getAvailabilityUseCase;

    @InjectMocks
    private AvailabilityController availabilityController;

    @Test
    void shouldReturnAvailabilityPassingExtractedBearerToken() {
        LocalDate date = LocalDate.of(2026, 11, 24);
        AvailabilityResponseDTO expected = new AvailabilityResponseDTO(
                202L,
                date,
                List.of(new AvailabilitySlotDTO(LocalTime.of(11, 30), LocalTime.of(12, 0)))
        );
        when(getAvailabilityUseCase.getAvailability(202L, date, "my-jwt")).thenReturn(expected);

        ResponseEntity<AvailabilityResponseDTO> response =
                availabilityController.getAvailability(202L, date, "Bearer my-jwt");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(getAvailabilityUseCase).getAvailability(202L, date, "my-jwt");
    }

    @Test
    void shouldPassNullJwtWhenAuthorizationHeaderIsMissingOrNotBearer() {
        LocalDate date = LocalDate.of(2026, 11, 24);
        AvailabilityResponseDTO expected = new AvailabilityResponseDTO(202L, date, List.of());
        when(getAvailabilityUseCase.getAvailability(202L, date, null)).thenReturn(expected);

        assertThat(availabilityController.getAvailability(202L, date, null).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(availabilityController.getAvailability(202L, date, "Basic abc").getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
