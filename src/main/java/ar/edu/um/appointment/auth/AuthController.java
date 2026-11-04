package ar.edu.um.appointment.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.registerUser(
                request.getLogin(),
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JWTTokenDTO> authorize(@Valid @RequestBody LoginRequestDTO request) {
        UserEntity user = authService.authenticate(request.getUsername(), request.getPassword());

        String jwt = tokenProvider.createToken(user);

        return ResponseEntity.ok(new JWTTokenDTO(jwt));
    }

    @GetMapping("/account")
    public ResponseEntity<AccountDTO> getAccount(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = jwt.getSubject();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        AccountDTO account = AccountDTO.builder()
                .login(user.getUsername())
                .email(user.getEmail())
                .activated(user.isActivated())
                .authorities(user.getAuthorities())
                .build();

        return ResponseEntity.ok(account);
    }
}