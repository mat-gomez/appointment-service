package ar.edu.um.appointment.auth;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String password;

    @Builder.Default
    @Column(name = "activated", nullable = false)
    private boolean activated = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_user_authority", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority")
    private Set<String> authorities;
}