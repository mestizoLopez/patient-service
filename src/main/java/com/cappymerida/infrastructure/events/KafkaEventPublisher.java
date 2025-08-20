package com.cappymerida.infrastructure.events;

import com.cappymerida.domain.events.EventPublisher;
import com.cappymerida.domain.records.BaseEvent;
import com.cappymerida.domain.records.PatientEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisher {

    private KafkaTemplate<String, String> kafkaTemplate;

    @Qualifier("dlqKafkaTemplate")
    private KafkaTemplate<String, String> dlqKafkaTemplate;

    private ObjectMapper objectMapper;

    @Value("${kafka.topics.patient-events}")
    private String patientEventsTopic;

    @Value("${kafka.topics.patient-events-dlq}")
    private String patientEventsDlqTopic;

    @Override
    public CompletableFuture<Void> publishPatientEvent(PatientEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.baseEvent().aggregateId();

            log.info("Publishing patient event: {} for patient: {}",
                    event.baseEvent().eventType(), key);
            log.debug("Event payload: {}", eventJson);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(patientEventsTopic, key, eventJson);

            return future.thenApply(result -> {
                log.info("Successfully published patient event: {} to partition: {} with offset: {}",
                        event.baseEvent().eventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                return null;
            });
            // Remove the exceptionally block - let errors propagate

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize patient event: {}", event.baseEvent().eventId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Void> publishPatientEventWithRetry(PatientEvent event, int maxRetries) {
        log.info("Publishing patient event with retry: {} (attempt)", event.baseEvent().eventId());
        return publishPatientEvent(event);
    }

    @Override
    public void publishPatientEventAsync(PatientEvent event) {
        publishPatientEvent(event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Async event publishing failed for event: {}",
                                event.baseEvent().eventId(), throwable);
                    } else {
                        log.debug("Async event publishing completed for event: {}",
                                event.baseEvent().eventId());
                    }
                });
    }

    @Override
    public CompletableFuture<Void> publishPatientEventsBatch(List<PatientEvent> events) {
        log.info("Publishing batch of {} patient events", events.size());

        List<CompletableFuture<Void>> futures = events.stream()
                .map(this::publishPatientEvent)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> log.info("Successfully published batch of {} events", events.size()))
                .exceptionally(throwable -> {
                    log.error("Failed to publish event batch", throwable);
                    return null; // Return null instead of throwing
                });
    }

    private void sendToDlq(PatientEvent event, Throwable throwable) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.baseEvent().aggregateId();

            // Agregar metadata del error
            Map<String, Object> errorMetadata = Map.of(
                    "originalError", throwable.getMessage(),
                    "errorTimestamp", LocalDateTime.now().toString(),
                    "retryCount", 0
            );

            // Crear evento con metadata del error
            var dlqEvent = new PatientEvent(
                    new BaseEvent(
                            event.baseEvent().eventId(),
                            event.baseEvent().eventType(),
                            event.baseEvent().aggregateId(),
                            event.baseEvent().aggregateType(),
                            event.baseEvent().version(),
                            event.baseEvent().timestamp(),
                            event.baseEvent().correlationId(),
                            event.baseEvent().causationId(),
                            event.baseEvent().userId(),
                            errorMetadata
                    ),
                    event.payload(),
                    event.occurredAt()
            );

            String dlqEventJson = objectMapper.writeValueAsString(dlqEvent);

            dlqKafkaTemplate.send(patientEventsDlqTopic, key, dlqEventJson)
                    .whenComplete((result, dlqThrowable) -> {
                        if (dlqThrowable != null) {
                            log.error("Failed to send event to DLQ: {}",
                                    event.baseEvent().eventId(), dlqThrowable);
                        } else {
                            log.warn("Event sent to DLQ: {} due to: {}",
                                    event.baseEvent().eventId(), throwable.getMessage());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for DLQ: {}", event.baseEvent().eventId(), e);
        }
    }
}
