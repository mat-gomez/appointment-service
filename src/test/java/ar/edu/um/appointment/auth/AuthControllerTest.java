package ar.edu.um.appointment.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        Mockito.reset(authService, tokenProvider, userRepository);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AuthService authServiceMock() {
            return mock(AuthService.class);
        }

        @Bean
        @Primary
        public TokenProvider tokenProviderMock() {
            return mock(TokenProvider.class);
        }

        @Bean
        @Primary
        public UserRepository userRepositoryMock() {
            return mock(UserRepository.class);
        }
    }

    @Test
    void register_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setLogin("newuser");
        request.setPassword("password123");
        request.setEmail("new@um.edu.ar");

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void authorize_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("melina");
        request.setPassword("password123");

        UserEntity mockUser = UserEntity.builder().username("melina").build();
        when(authService.authenticate(anyString(), anyString())).thenReturn(mockUser);
        when(tokenProvider.createToken(any(UserEntity.class))).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idToken").value("mocked-jwt-token"));
    }

    @Test
    void getAccount_ShouldReturnAccountDetails() throws Exception {
        UserEntity mockUser = UserEntity.builder()
                .username("melina")
                .email("mat.gomez@alumno.um.edu.ar")
                .activated(true)
                .authorities(Set.of("ROLE_USER"))
                .build();

        when(userRepository.findByUsername("melina")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/account")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.subject("melina"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("melina"))
                .andExpect(jsonPath("$.email").value("mat.gomez@alumno.um.edu.ar"));
    }
}