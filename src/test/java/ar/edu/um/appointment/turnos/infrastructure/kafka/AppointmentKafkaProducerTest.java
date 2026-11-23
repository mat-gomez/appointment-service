package ar.edu.um.appointment.turnos.infrastructure.kafka;

import ar.edu.um.appointment.turnos.domain.model.AdditionalInformationSubmittedEvent;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationRepository;
import ar.edu.um.appointment.web.errors.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentKafkaProducerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-11-23T18:00:00Z");
    private static final String TOPIC = "catedra.turnos.telefono.grupo-test";

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ReservationRepository reservationRepository;

    private AppointmentKafkaProducer producer;

    @BeforeEach
    void setUp() {
        producer = new AppointmentKafkaProducer(
                kafkaTemplate,
                reservationRepository,
                TOPIC,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldNormalizePhoneAndPublishEventWithReservationKey() {
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("proc-100")
                .username("melina")
                .build();
        when(reservationRepository.findByReservationId("proc-100")).thenReturn(Optional.of(entity));

        AdditionalInformationSubmittedEvent event =
                producer.submitPhone("proc-100", "+54 (261) 555-1234.", "melina");

        assertThat(event.phone()).isEqualTo("+542615551234");
        assertThat(event.reservationProcessId()).isEqualTo("proc-100");
        assertThat(event.occurredAt()).isEqualTo(FIXED_NOW);
        assertThat(event.eventId()).isNotBlank();
        verify(kafkaTemplate).send(TOPIC, "proc-100", event);
    }

    @Test
    void shouldRejectInvalidPhoneNumber() {
        assertThatThrownBy(() -> producer.submitPhone("proc-100", "abc-12", "melina"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> producer.submitPhone("proc-100", "   ", "melina"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void shouldRejectWhenReservationIdIsBlankOrBelongsToAnotherUser() {
        assertThatThrownBy(() -> producer.submitPhone("", "+542615551234", "melina"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        ReservationEntity otherUserEntity = ReservationEntity.builder()
                .reservationId("proc-200")
                .username("otro_usuario")
                .build();
        when(reservationRepository.findByReservationId("proc-200")).thenReturn(Optional.of(otherUserEntity));

        assertThatThrownBy(() -> producer.submitPhone("proc-200", "+542615551234", "melina"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        verifyNoInteractions(kafkaTemplate);
    }
}
