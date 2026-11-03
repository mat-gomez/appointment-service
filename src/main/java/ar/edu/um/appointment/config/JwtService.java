package ar.edu.um.appointment.config;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class JwtService {

    public String extractUsername(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        return jwt.getSubject() != null ? jwt.getSubject() : jwt.getClaimAsString("preferred_username");
    }

    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(Jwt jwt) {
        if (jwt == null) {
            return Collections.emptyList();
        }
        Object roles = jwt.getClaim("roles");
        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }
}