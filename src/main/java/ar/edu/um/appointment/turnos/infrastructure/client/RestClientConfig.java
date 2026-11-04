package ar.edu.um.appointment.turnos.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${application.client.catalog.url:http://localhost:8080}")
    private String catalogServiceUrl;

    @Value("${application.client.catedra.url:http://172.17.201.1:8090}")
    private String catedraServiceUrl;

    @Bean
    public RestClient catalogRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(catalogServiceUrl)
                .requestFactory(factory)
                .build();
    }

    @Bean
    public RestClient catedraRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(catedraServiceUrl)
                .requestFactory(factory)
                .build();
    }
}