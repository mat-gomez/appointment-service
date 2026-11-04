package ar.edu.um.appointment.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.registerUser(
                request.getLogin(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JWTTokenDTO> authorize(@Valid @RequestBody LoginRequestDTO request) {
        UserEntity user = authService.authenticate(request.getUsername(), request.getPassword());
        // Simulación
        String jwt = "dummy-jwt-token-for-" + user.getUsername();
        return ResponseEntity.ok(new JWTTokenDTO(jwt));
    }

    @GetMapping("/account")
    public ResponseEntity<AccountDTO> getAccount() {
        AccountDTO account = AccountDTO.builder()
                .login("melina")
                .email("mat.gomez@alumno.um.edu.ar")
                .firstName("Melina")
                .lastName("Gomez Torres")
                .activated(true)
                .authorities(java.util.Set.of("ROLE_USER"))
                .build();
        return ResponseEntity.ok(account);
    }
}