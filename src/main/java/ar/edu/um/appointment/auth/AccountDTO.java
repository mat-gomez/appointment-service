package ar.edu.um.appointment.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private String login;
    private String email;
    private String firstName;
    private String lastName;
    private boolean activated;
    private String imageUrl;
    private String langKey;
    private Set<String> authorities;
}