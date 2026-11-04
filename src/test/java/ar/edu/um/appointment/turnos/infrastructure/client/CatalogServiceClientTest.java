package ar.edu.um.appointment.turnos.infrastructure.client;

import ar.edu.um.appointment.web.errors.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CatalogServiceClientTest {

    private CatalogServiceClient catalogServiceClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://localhost:8080").build();
        catalogServiceClient = new CatalogServiceClient(restClient);
    }

    @Test
    void isProfessionalEnabled_ShouldReturnTrue_WhenServiceRespondsTrue() {
        mockServer.expect(requestTo("http://localhost:8080/api/professionals/1/enabled"))
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

        boolean result = catalogServiceClient.isProfessionalEnabled(1L);

        assertTrue(result);
        mockServer.verify();
    }

    @Test
    void isProfessionalEnabled_ShouldThrow503_WhenServiceFails() {
        mockServer.expect(requestTo("http://localhost:8080/api/professionals/1/enabled"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        DomainException exception = assertThrows(DomainException.class, () ->
                catalogServiceClient.isProfessionalEnabled(1L)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        assertEquals("CATALOG_SERVICE_UNAVAILABLE", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void getWeeklySchedules_ShouldReturnData_WhenServiceResponds() {
        mockServer.expect(requestTo("http://localhost:8080/api/professionals/1/weekly-schedules"))
                .andRespond(withSuccess("[{\"day\":\"MONDAY\"}]", MediaType.APPLICATION_JSON));

        Object result = catalogServiceClient.getWeeklySchedules(1L);

        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void getWeeklySchedules_ShouldThrow503_WhenServiceFails() {
        mockServer.expect(requestTo("http://localhost:8080/api/professionals/1/weekly-schedules"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        DomainException exception = assertThrows(DomainException.class, () ->
                catalogServiceClient.getWeeklySchedules(1L)
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        assertEquals("CATALOG_SERVICE_UNAVAILABLE", exception.getMessage());
        mockServer.verify();
    }
}