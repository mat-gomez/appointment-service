package ar.edu.um.appointment.turnos.infrastructure.client;

import ar.edu.um.appointment.turnos.domain.model.WeeklyScheduleDTO;
import ar.edu.um.appointment.web.errors.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CatalogServiceClientTest {

    private MockRestServiceServer server;
    private CatalogServiceClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8081");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new CatalogServiceClient(builder.build());
    }

    @Test
    void shouldReturnTrueWhenProfessionalIsEnabled() {
        server.expect(requestTo("http://localhost:8081/api/professionals/202/enabled"))
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

        assertThat(client.isProfessionalEnabled(202L)).isTrue();
        server.verify();
    }

    @Test
    void shouldReturnWeeklySchedulesWithAuthorizationHeader() {
        String json = """
                [
                  {
                    "id": 303,
                    "professionalId": 202,
                    "dayOfWeek": "TUESDAY",
                    "startTime": "09:00:00",
                    "endTime": "11:00:00",
                    "slotDurationMinutes": 30,
                    "enabled": true
                  }
                ]
                """;
        server.expect(requestTo("http://localhost:8081/api/catalog/professionals/202/schedules"))
                .andExpect(header("Authorization", "Bearer user-jwt"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<WeeklyScheduleDTO> schedules = client.fetchWeeklySchedules(202L, "user-jwt");

        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).id()).isEqualTo(303L);
        server.verify();
    }

    @Test
    void shouldThrowServiceUnavailableWhenCatalogFails() {
        server.expect(requestTo("http://localhost:8081/api/professionals/202/enabled"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.isProfessionalEnabled(202L))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        server.verify();
    }
}
