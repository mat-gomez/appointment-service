package ar.edu.um.appointment.turnos.domain;

import java.time.Instant;

public class ReservationProcess {

    private String reservationProcessId;
    private String holdId;
    private Long professionalId;
    private String externalPatientId;
    private String status;
    private Instant expiresAt;

    public ReservationProcess(String reservationProcessId, String holdId, Long professionalId, String externalPatientId, String status, Instant expiresAt) {
        this.reservationProcessId = reservationProcessId;
        this.holdId = holdId;
        this.professionalId = professionalId;
        this.externalPatientId = externalPatientId;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public String getReservationProcessId() {
        return reservationProcessId;
    }

    public String getHoldId() {
        return holdId;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public String getExternalPatientId() {
        return externalPatientId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
