package ar.edu.um.appointment.auth;

import ar.edu.um.appointment.web.errors.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    public UserEntity registerUser(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new DomainException("USERNAME_ALREADY_EXISTS", HttpStatus.BAD_REQUEST, "Username already used");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DomainException("EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST, "Email already used");
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
                .orElseThrow(() -> new DomainException("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new DomainException("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return user;
    }
}