package ar.edu.um.appointment.turnos.infrastructure.client;

import ar.edu.um.appointment.turnos.domain.model.ExternalAppointmentDTO;
import ar.edu.um.appointment.turnos.domain.model.HoldResultDTO;
import ar.edu.um.appointment.turnos.domain.model.OccupancyDTO;
import ar.edu.um.appointment.web.errors.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CatedraAppointmentsClientTest {

    private MockRestServiceServer server;
    private CatedraAppointmentsClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8080");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new CatedraAppointmentsClient(builder.build());
    }

    @Test
    void shouldFetchOccupanciesForDateRange() {
        LocalDate date = LocalDate.of(2026, 11, 24);
        String json = """
                {
                  "occupancies": [
                    {
                      "date": "2026-11-24",
                      "startTime": "09:00:00",
                      "status": "CONFIRMED",
                      "expiresAt": null
                    }
                  ]
                }
                """;
        server.expect(requestTo("http://localhost:8080/api/appointment-occupancies?professionalId=202&from=2026-11-24&to=2026-11-24"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<OccupancyDTO> occupancies = client.fetchOccupancies(202L, date, date);

        assertThat(occupancies).hasSize(1);
        assertThat(occupancies.get(0).status()).isEqualTo("CONFIRMED");
        server.verify();
    }

    @Test
    void shouldCreateAndConfirmHoldReturningConfirmedProcessId() {
        LocalDate date = LocalDate.of(2026, 11, 24);
        LocalTime time = LocalTime.of(9, 30);
        Instant expiresAt = Instant.parse("2026-11-24T12:35:00Z");

        server.expect(requestTo("http://localhost:8080/api/appointment-holds"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"holdId\":\"hold-99\",\"expiresAt\":\"" + expiresAt + "\"}",
                        MediaType.APPLICATION_JSON
                ));
        server.expect(requestTo("http://localhost:8080/api/appointment-holds/hold-99/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"reservationProcessId\":\"process-99\",\"status\":\"WAITING_FOR_PHONE\"}"));

        HoldResultDTO hold = client.createHold(202L, date, time, "patient-99", "proc-99");
        assertThat(hold.holdId()).isEqualTo("hold-99");
        assertThat(hold.expiresAt()).isEqualTo(expiresAt);

        String confirmedId = client.confirmHold("hold-99", "patient-99", "proc-99");
        assertThat(confirmedId).isEqualTo("process-99");
        server.verify();
    }

    @Test
    void shouldFallbackToOriginalProcessIdWhenConfirmResponseHasNoProcessId() {
        server.expect(requestTo("http://localhost:8080/api/appointment-holds/hold-99/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{}"));

        String confirmedId = client.confirmHold("hold-99", "patient-99", "proc-99");
        assertThat(confirmedId).isEqualTo("proc-99");
        server.verify();
    }

    @Test
    void shouldCancelConfirmedAppointmentPropagatingJwt() {
        server.expect(requestTo("http://localhost:8080/api/appointments/hold-99/cancel"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Bearer user-jwt"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        client.cancelConfirmedAppointment("proc-99", "hold-99", "user-jwt");
        server.verify();
    }

    @Test
    void shouldFetchAppointmentsForReconciliation() {
        String json = """
                [
                  {
                    "id": "ext-1",
                    "reservationProcessId": "proc-99",
                    "holdId": "hold-99",
                    "status": "CONFIRMED"
                  }
                ]
                """;
        server.expect(requestTo("http://localhost:8080/api/appointments"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<ExternalAppointmentDTO> appointments = client.fetchAppointments();

        assertThat(appointments).hasSize(1);
        assertThat(appointments.get(0).reservationProcessId()).isEqualTo("proc-99");
        server.verify();
    }

    @Test
    void shouldThrowServiceUnavailableWhenHoldCreationFails() {
        LocalDate date = LocalDate.of(2026, 11, 24);
        LocalTime time = LocalTime.of(9, 30);

        server.expect(requestTo("http://localhost:8080/api/appointment-holds"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.createHold(202L, date, time, "patient-99", "proc-99"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        server.verify();
    }
}
