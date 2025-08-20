package com.cappymerida.infrastructure.events;

import com.cappymerida.domain.records.PatientEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "kafka.event.publishing.enabled", havingValue = "true")
public class PatientEventListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.patient-events}",
            groupId = "${spring.kafka.consumer.group-id}-listener",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePatientEvent(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        try {
            log.info("Received patient event from topic: {}, partition: {}, offset: {}, key: {}",
                    topic, partition, offset, key);

            PatientEvent event = objectMapper.readValue(eventPayload, PatientEvent.class);

            log.info("Processing patient event: {} for patient: {} by user: {}",
                    event.baseEvent().eventType(),
                    event.baseEvent().aggregateId(),
                    event.baseEvent().userId());

            // Aquí puedes agregar lógica de procesamiento adicional
            processPatientEvent(event);

            // Confirmar procesamiento
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing patient event from topic: {}, payload: {}", topic, eventPayload, e);
            // No hacer ack() para reintentarlo
        }
    }

    private void processPatientEvent(PatientEvent event) {
        switch (event.baseEvent().eventType()) {
            case PATIENT_REGISTERED -> handlePatientRegistered(event);
            case PATIENT_UPDATED -> handlePatientUpdated(event);
            case PATIENT_DEACTIVATED -> handlePatientDeactivated(event);
            case PATIENT_ACTIVATED -> handlePatientActivated(event);
            case PATIENT_DELETED -> handlePatientDeleted(event);
            default -> log.warn("Unknown event type: {}", event.baseEvent().eventType());
        }
    }

    private void handlePatientRegistered(PatientEvent event) {
        log.info("Patient registered: {} - {}",
                event.payload().patientId(),
                event.payload().demographics().getFullName());

        // Aquí podrías:
        // - Enviar notificación de bienvenida
        // - Crear entrada en sistema de auditoría
        // - Sincronizar con sistemas externos
    }

    private void handlePatientUpdated(PatientEvent event) {
        log.info("Patient updated: {} - Changes: {}",
                event.payload().patientId(),
                event.payload().changeReason());

        // Aquí podrías:
        // - Notificar cambios a sistemas dependientes
        // - Actualizar caches
        // - Log de auditoría detallado
    }

    private void handlePatientDeactivated(PatientEvent event) {
        log.info("Patient deactivated: {}", event.payload().patientId());

        // Aquí podrías:
        // - Cancelar citas futuras
        // - Notificar a providers
        // - Archivar datos
    }

    private void handlePatientActivated(PatientEvent event) {
        log.info("Patient activated: {}", event.payload().patientId());

        // Aquí podrías:
        // - Restaurar accesos
        // - Notificar reactivación
    }

    private void handlePatientDeleted(PatientEvent event) {
        log.warn("Patient deleted: {} - Reason: {}",
                event.payload().patientId(),
                event.payload().changeReason());

        // Aquí podrías:
        // - Cleanup de datos relacionados
        // - Auditoría de eliminación
        // - Notificaciones críticas
    }
}