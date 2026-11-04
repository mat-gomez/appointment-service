package ar.edu.um.appointment.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

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

        Exception exception = assertThrows(RuntimeException.class, () ->
                authService.registerUser("existing", "new@um.edu.ar", "password123")
        );

        assertEquals("USERNAME_ALREADY_EXISTS", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@um.edu.ar")).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () ->
                authService.registerUser("newuser", "existing@um.edu.ar", "password123")
        );

        assertEquals("EMAIL_ALREADY_EXISTS", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}