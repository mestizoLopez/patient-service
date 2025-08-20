package com.cappymerida.application.service;

import com.cappymerida.domain.events.EventCorrelationService;
import com.cappymerida.domain.events.EventValidationService;
import com.cappymerida.domain.records.BaseEvent;
import com.cappymerida.domain.records.PatientEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PatientDomainEventService implements EventCorrelationService, EventValidationService {

    @Override
    public String generateCorrelationId() {
        return "CORR-" + UUID.randomUUID().toString();
    }

    @Override
    public PatientEvent correlateEvent(PatientEvent originalEvent, PatientEvent newEvent) {
        BaseEvent correlatedBaseEvent = new BaseEvent(
                newEvent.baseEvent().eventId(),
                newEvent.baseEvent().eventType(),
                newEvent.baseEvent().aggregateId(),
                newEvent.baseEvent().aggregateType(),
                newEvent.baseEvent().version(),
                newEvent.baseEvent().timestamp(),
                originalEvent.baseEvent().correlationId(), // Usar correlationId del evento original
                originalEvent.baseEvent().eventId(), // causationId es el eventId del evento original
                newEvent.baseEvent().userId(),
                newEvent.baseEvent().metadata()
        );

        return new PatientEvent(correlatedBaseEvent, newEvent.payload(), newEvent.occurredAt());
    }

    @Override
    public boolean areCorrelated(PatientEvent event1, PatientEvent event2) {
        if (event1.baseEvent().correlationId() == null || event2.baseEvent().correlationId() == null) {
            return false;
        }
        return event1.baseEvent().correlationId().equals(event2.baseEvent().correlationId());
    }

    @Override
    public void validateEvent(PatientEvent event) {
        ValidationResult result = isValidEvent(event);
        if (!result.isValid()) {
            throw new IllegalArgumentException("Invalid event: " + result.errorMessage());
        }
    }

    @Override
    public ValidationResult isValidEvent(PatientEvent event) {
        List<String> errors = new java.util.ArrayList<>();

        // Validar BaseEvent
        if (event.baseEvent() == null) {
            errors.add("BaseEvent cannot be null");
        } else {
            if (event.baseEvent().eventId() == null || event.baseEvent().eventId().isBlank()) {
                errors.add("EventId cannot be null or blank");
            }
            if (event.baseEvent().eventType() == null) {
                errors.add("EventType cannot be null");
            }
            if (event.baseEvent().aggregateId() == null || event.baseEvent().aggregateId().isBlank()) {
                errors.add("AggregateId cannot be null or blank");
            }
            if (event.baseEvent().timestamp() == null) {
                errors.add("Timestamp cannot be null");
            }
        }

        // Validar Payload
        if (event.payload() == null) {
            errors.add("Payload cannot be null");
        } else {
            if (event.payload().patientId() == null || event.payload().patientId().isBlank()) {
                errors.add("Patient ID in payload cannot be null or blank");
            }
            if (event.payload().demographics() == null) {
                errors.add("Demographics in payload cannot be null");
            }
        }

        // Validar consistencia entre BaseEvent y Payload
        if (event.baseEvent() != null && event.payload() != null) {
            if (!event.baseEvent().aggregateId().equals(event.payload().patientId())) {
                errors.add("AggregateId must match PatientId in payload");
            }
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            return ValidationResult.invalid("Event validation failed", errors);
        }
    }
}
