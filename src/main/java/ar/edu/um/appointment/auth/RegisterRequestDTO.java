package ar.edu.um.appointment.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    @NotBlank
    @Size(min = 3, max = 50)
    private String login;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;

    private String firstName;
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    private String imageUrl;
    private String langKey;
}