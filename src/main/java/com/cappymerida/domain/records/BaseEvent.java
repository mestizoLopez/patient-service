package com.cappymerida.domain.records;

import com.cappymerida.domain.enums.EventType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record BaseEvent(
        String eventId,
        EventType eventType,
        String aggregateId,
        String aggregateType,
        Long version,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        String correlationId,
        String causationId,
        String userId,
        Map<String, Object> metadata
) {

    /**
     * Factory method para crear un BaseEvent con valores por defecto
     */
    public static BaseEvent create(EventType eventType, String aggregateId, String aggregateType,
                                   Long version, String userId, Map<String, Object> metadata) {
        return new BaseEvent(
                UUID.randomUUID().toString(),
                eventType,
                aggregateId,
                aggregateType,
                version,
                LocalDateTime.now(),
                UUID.randomUUID().toString(), // correlationId único por defecto
                null, // causationId null para eventos iniciales
                userId,
                metadata != null ? metadata : Map.of()
        );
    }

    /**
     * Crea una copia del evento con un nuevo causationId
     */
    public BaseEvent withCausationId(String causationId) {
        return new BaseEvent(
                this.eventId,
                this.eventType,
                this.aggregateId,
                this.aggregateType,
                this.version,
                this.timestamp,
                this.correlationId,
                causationId,
                this.userId,
                this.metadata
        );
    }

    /**
     * Crea una copia del evento con nueva correlación
     */
    public BaseEvent withCorrelation(String correlationId, String causationId) {
        return new BaseEvent(
                this.eventId,
                this.eventType,
                this.aggregateId,
                this.aggregateType,
                this.version,
                this.timestamp,
                correlationId,
                causationId,
                this.userId,
                this.metadata
        );
    }

    /**
     * Añade metadata adicional al evento
     */
    public BaseEvent withAdditionalMetadata(Map<String, Object> additionalMetadata) {
        Map<String, Object> newMetadata = new java.util.HashMap<>(this.metadata);
        newMetadata.putAll(additionalMetadata);

        return new BaseEvent(
                this.eventId,
                this.eventType,
                this.aggregateId,
                this.aggregateType,
                this.version,
                this.timestamp,
                this.correlationId,
                this.causationId,
                this.userId,
                Map.copyOf(newMetadata)
        );
    }
}
