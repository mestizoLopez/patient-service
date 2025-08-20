package com.cappymerida.domain.events;

import com.cappymerida.domain.records.PatientEvent;

public interface DomainEventHandler {

    /**
     * Maneja un evento de paciente
     * @param event Evento a manejar
     */
    void handle(PatientEvent event);

    /**
     * Indica si este handler puede manejar el tipo de evento dado
     * @param event Evento a verificar
     * @return true si puede manejar el evento
     */
    boolean canHandle(PatientEvent event);

    /**
     * Obtiene la prioridad del handler (menor número = mayor prioridad)
     * @return prioridad del handler
     */
    default int getPriority() {
        return 100;
    }
}
