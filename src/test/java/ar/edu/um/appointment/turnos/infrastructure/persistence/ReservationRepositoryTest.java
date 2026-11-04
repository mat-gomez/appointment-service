package ar.edu.um.appointment.turnos.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void shouldSaveAndFindReservationEntity() {
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("res-12345")
                .professionalId(10L)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .status(ReservationStatus.HOLD_CREATED)
                .build();

        ReservationEntity saved = reservationRepository.save(entity);

        Optional<ReservationEntity> found = reservationRepository.findByReservationId("res-12345");

        assertThat(found).isPresent();
        assertThat(found.get().getReservationId()).isEqualTo("res-12345");
        assertThat(found.get().getStatus()).isEqualTo(ReservationStatus.HOLD_CREATED);
    }
}