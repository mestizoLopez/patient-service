package com.cappymerida.domain.events;

import com.cappymerida.domain.records.PatientEvent;

public interface EventCorrelationService {

    /**
     * Crea un nuevo correlation ID para una nueva cadena de eventos
     * @return nuevo correlation ID
     */
    String generateCorrelationId();

    /**
     * Crea un evento correlacionado basado en otro evento
     * @param originalEvent Evento original
     * @param newEvent Nuevo evento a correlacionar
     * @return evento con correlación establecida
     */
    PatientEvent correlateEvent(PatientEvent originalEvent, PatientEvent newEvent);

    /**
     * Verifica si dos eventos están correlacionados
     * @param event1 Primer evento
     * @param event2 Segundo evento
     * @return true si están correlacionados
     */
    boolean areCorrelated(PatientEvent event1, PatientEvent event2);
}
