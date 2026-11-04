package ar.edu.um.appointment.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserEntity registerUser(String username, String email, String rawPassword, String firstName, String lastName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("USERNAME_ALREADY_EXISTS");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        UserEntity user = UserEntity.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .activated(true)
                .authorities(Set.of("ROLE_USER"))
                .build();

        return userRepository.save(user);
    }

    public UserEntity authenticate(String username, String rawPassword) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        return user;
    }
}