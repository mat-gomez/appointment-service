package ar.edu.um.appointment.auth;

import ar.edu.um.appointment.web.errors.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_ShouldSucceed_WhenDataIsUnique() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@um.edu.ar")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = authService.registerUser("newuser", "new@um.edu.ar", "password123");

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("encoded_pass", result.getPassword());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        DomainException exception = assertThrows(DomainException.class, () ->
                authService.registerUser("existing", "new@um.edu.ar", "password123")
        );

        assertEquals("USERNAME_ALREADY_EXISTS", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@um.edu.ar")).thenReturn(true);

        DomainException exception = assertThrows(DomainException.class, () ->
                authService.registerUser("newuser", "existing@um.edu.ar", "password123")
        );

        assertEquals("EMAIL_ALREADY_EXISTS", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void authenticate_ShouldSucceed_WhenCredentialsAreValid() {
        UserEntity mockUser = UserEntity.builder()
                .username("melina")
                .password("encoded_pass")
                .build();

        when(userRepository.findByUsername("melina")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encoded_pass")).thenReturn(true);

        UserEntity result = authService.authenticate("melina", "password123");

        assertNotNull(result);
        assertEquals("melina", result.getUsername());
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class, () ->
                authService.authenticate("unknown", "password123")
        );

        assertEquals("INVALID_CREDENTIALS", exception.getMessage());
    }

    @Test
    void authenticate_ShouldThrowException_WhenPasswordIsInvalid() {
        UserEntity mockUser = UserEntity.builder()
                .username("melina")
                .password("encoded_pass")
                .build();

        when(userRepository.findByUsername("melina")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong_password", "encoded_pass")).thenReturn(false);

        DomainException exception = assertThrows(DomainException.class, () ->
                authService.authenticate("melina", "wrong_password")
        );

        assertEquals("INVALID_CREDENTIALS", exception.getMessage());
    }
}