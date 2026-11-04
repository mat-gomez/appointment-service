package ar.edu.um.appointment.turnos.infrastructure.client;

import ar.edu.um.appointment.web.errors.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CatedraAppointmentsClientTest {

    private CatedraAppointmentsClient catedraAppointmentsClient;
    private MockRestServiceServer mockServer;
    private final String token = "test-jwt-token";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://172.17.201.1:8090").build();
        catedraAppointmentsClient = new CatedraAppointmentsClient(restClient);
    }

    @Test
    void getAppointmentOccupancies_ShouldReturnData_WhenServiceResponds() {
        mockServer.expect(requestTo("http://172.17.201.1:8090/api/appointment-occupancies?professionalId=1"))
                .andExpect(header("Authorization", "Bearer " + token))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Object result = catedraAppointmentsClient.getAppointmentOccupancies(token, 1L);

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void createAppointmentHold_ShouldSucceed_WhenValid() {
        mockServer.expect(requestTo("http://172.17.201.1:8090/api/appointment-holds"))
                .andExpect(header("Authorization", "Bearer " + token))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        Object result = catedraAppointmentsClient.createAppointmentHold(token, "payload");

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void confirmAppointment_ShouldSucceed_WhenValid() {
        mockServer.expect(requestTo("http://172.17.201.1:8090/api/appointments/1/confirm"))
                .andExpect(header("Authorization", "Bearer " + token))
                .andRespond(withSuccess("{\"status\":\"CONFIRMED\"}", MediaType.APPLICATION_JSON));

        Object result = catedraAppointmentsClient.confirmAppointment(token, 1L);

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void createAppointment_ShouldSucceed_WhenValid() {
        mockServer.expect(requestTo("http://172.17.201.1:8090/api/appointments"))
                .andExpect(header("Authorization", "Bearer " + token))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        Object result = catedraAppointmentsClient.createAppointment(token, "payload");

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void cancelAppointment_ShouldSucceed_WhenValid() {
        mockServer.expect(requestTo("http://172.17.201.1:8090/api/appointments/1/cancel"))
                .andExpect(header("Authorization", "Bearer " + token))
                .andRespond(withStatus(HttpStatus.OK));

        assertDoesNotThrow(() -> catedraAppointmentsClient.cancelAppointment(token, 1L));
        mockServer.verify();
    }

    @Test
    void clientMethods_ShouldThrow503_WhenServiceFails() {
        mockServer.expect(requestTo("http://172.17.201.1:8090/api/appointments/1/cancel"))
                .andExpect(header("Authorization", "Bearer " + token))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        DomainException exception = assertThrows(DomainException.class, () ->
                catedraAppointmentsClient.cancelAppointment(token, 1L)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        assertEquals("CATEDRA_SERVICE_UNAVAILABLE", exception.getMessage());
        mockServer.verify();
    }
}