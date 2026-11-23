package ar.edu.um.appointment.turnos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    Optional<ReservationEntity> findByReservationId(String reservationId);
    List<ReservationEntity> findByUsernameAndStatusIn(String username, Collection<ReservationStatus> statuses);
    List<ReservationEntity> findAllByUsernameOrderByCreatedAtDesc(String username);
    List<ReservationEntity> findAllByStatusIn(Collection<ReservationStatus> statuses);
}
