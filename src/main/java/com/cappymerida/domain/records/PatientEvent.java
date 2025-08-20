package com.cappymerida.domain.records;

import com.cappymerida.domain.enums.EventType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

public record PatientEvent(
        BaseEvent baseEvent,
        PatientEventPayload payload,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime occurredAt
) {

    /**
     * Factory method para crear un evento de paciente básico
     */
    public static PatientEvent create(EventType eventType, PatientEventPayload payload,
                                      String userId, Map<String, Object> metadata) {
        BaseEvent baseEvent = BaseEvent.create(
                eventType,
                payload.patientId(),
                "Patient",
                payload.patientVersion(),
                userId,
                metadata
        );

        return new PatientEvent(baseEvent, payload, LocalDateTime.now());
    }

    /**
     * Factory method para crear un evento con correlación específica
     */
    public static PatientEvent createWithCorrelation(EventType eventType, PatientEventPayload payload,
                                                     String userId, Map<String, Object> metadata,
                                                     String correlationId, String causationId) {
        BaseEvent baseEvent = BaseEvent.create(
                eventType,
                payload.patientId(),
                "Patient",
                payload.patientVersion(),
                userId,
                metadata
        );

        BaseEvent correlatedEvent = baseEvent.withCorrelation(correlationId, causationId);

        return new PatientEvent(correlatedEvent, payload, LocalDateTime.now());
    }

    /**
     * Factory method para crear un evento hijo correlacionado con otro evento
     */
    public static PatientEvent createCorrelatedChild(PatientEvent parentEvent, EventType eventType,
                                                     PatientEventPayload payload, String userId,
                                                     Map<String, Object> metadata) {
        BaseEvent baseEvent = BaseEvent.create(
                eventType,
                payload.patientId(),
                "Patient",
                payload.patientVersion(),
                userId,
                metadata
        );

        BaseEvent correlatedEvent = baseEvent.withCorrelation(
                parentEvent.baseEvent().correlationId(),
                parentEvent.baseEvent().eventId()
        );

        return new PatientEvent(correlatedEvent, payload, LocalDateTime.now());
    }

    /**
     * Crea una copia del evento con metadata adicional
     */
    public PatientEvent withAdditionalMetadata(Map<String, Object> additionalMetadata) {
        BaseEvent updatedBaseEvent = this.baseEvent.withAdditionalMetadata(additionalMetadata);
        return new PatientEvent(updatedBaseEvent, this.payload, this.occurredAt);
    }

    /**
     * Obtiene el ID del paciente del evento
     */
    public String getPatientId() {
        return this.baseEvent.aggregateId();
    }

    /**
     * Obtiene el tipo de evento
     */
    public EventType getEventType() {
        return this.baseEvent.eventType();
    }

    /**
     * Obtiene el ID único del evento
     */
    public String getEventId() {
        return this.baseEvent.eventId();
    }

    /**
     * Verifica si este evento está correlacionado con otro
     */
    public boolean isCorrelatedWith(PatientEvent other) {
        return this.baseEvent.correlationId() != null &&
                this.baseEvent.correlationId().equals(other.baseEvent().correlationId());
    }

    /**
     * Verifica si este evento fue causado por otro evento específico
     */
    public boolean wasCausedBy(PatientEvent other) {
        return this.baseEvent.causationId() != null &&
                this.baseEvent.causationId().equals(other.baseEvent().eventId());
    }

    /**
     * Obtiene una representación resumida del evento para logging
     */
    public String getSummary() {
        return String.format("PatientEvent[%s] - %s for patient %s by %s",
                this.baseEvent.eventType(),
                this.payload.changeReason(),
                this.payload.getPatientFullName(),
                this.baseEvent.userId()
        );
    }

    /**
     * Verifica si el evento es de un tipo específico
     */
    public boolean isOfType(EventType eventType) {
        return this.baseEvent.eventType() == eventType;
    }

    /**
     * Verifica si el evento pertenece a un paciente específico
     */
    public boolean belongsToPatient(String patientId) {
        return this.baseEvent.aggregateId().equals(patientId);
    }
}
