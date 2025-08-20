package com.cappymerida.domain.events;

import com.cappymerida.domain.records.PatientEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EventPublisher {

    /**
     * Publica un evento de paciente de forma síncrona
     * @param event Evento a publicar
     * @return Future que se completa cuando el evento es publicado
     */
    CompletableFuture<Void> publishPatientEvent(PatientEvent event);

    /**
     * Publica un evento de paciente con reintentos
     * @param event Evento a publicar
     * @param maxRetries Número máximo de reintentos
     * @return Future que se completa cuando el evento es publicado
     */
    CompletableFuture<Void> publishPatientEventWithRetry(PatientEvent event, int maxRetries);

    /**
     * Publica un evento de paciente de forma asíncrona (fire and forget)
     * @param event Evento a publicar
     */
    void publishPatientEventAsync(PatientEvent event);

    /**
     * Publica múltiples eventos en lote
     * @param events Lista de eventos a publicar
     * @return Future que se completa cuando todos los eventos son publicados
     */
    CompletableFuture<Void> publishPatientEventsBatch(List<PatientEvent> events);
}
