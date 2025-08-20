package com.cappymerida.infrastructure.events;

import com.cappymerida.domain.records.AuditEvent;
import com.cappymerida.domain.records.PatientEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "kafka.event.publishing.enabled", havingValue = "true")
public class AuditEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.audit-events}")
    private String auditEventsTopic;

    public void publishAuditEvent(PatientEvent patientEvent) {
        try {
            AuditEvent auditEvent = createAuditEvent(patientEvent);
            String auditJson = objectMapper.writeValueAsString(auditEvent);

            kafkaTemplate.send(auditEventsTopic, patientEvent.baseEvent().aggregateId(), auditJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.debug("Audit event published successfully for patient: {}",
                                    patientEvent.baseEvent().aggregateId());
                        } else {
                            log.error("Failed to publish audit event for patient: {}",
                                    patientEvent.baseEvent().aggregateId(), ex);
                        }
                    });

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit event for patient: {}",
                    patientEvent.baseEvent().aggregateId(), e);
        }
    }

    private AuditEvent createAuditEvent(PatientEvent patientEvent) {
        return new AuditEvent(
                patientEvent.baseEvent().eventId(),
                "PATIENT_EVENT",
                patientEvent.baseEvent().eventType().toString(),
                patientEvent.baseEvent().aggregateId(),
                patientEvent.baseEvent().userId(),
                LocalDateTime.now(),
                Map.of(
                        "originalEventType", patientEvent.baseEvent().eventType(),
                        "aggregateType", patientEvent.baseEvent().aggregateType(),
                        "version", patientEvent.baseEvent().version(),
                        "correlationId", patientEvent.baseEvent().correlationId()
                )
        );
    }

}
