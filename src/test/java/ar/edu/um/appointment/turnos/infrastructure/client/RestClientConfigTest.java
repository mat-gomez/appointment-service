package ar.edu.um.appointment.turnos.infrastructure.client;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestClientConfigTest {

    @Test
    void restClients_ShouldBeInitialized() {
        RestClientConfig config = new RestClientConfig();

        RestClient catalogClient = config.catalogRestClient();
        RestClient catedraClient = config.catedraRestClient();

        assertNotNull(catalogClient);
        assertNotNull(catedraClient);
    }
}