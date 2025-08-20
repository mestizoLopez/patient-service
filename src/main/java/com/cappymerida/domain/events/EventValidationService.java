package com.cappymerida.domain.events;

import com.cappymerida.domain.records.PatientEvent;

public interface EventValidationService {

    /**
     * Valida que un evento esté correctamente formado
     * @param event Evento a validar
     * @throws IllegalArgumentException si el evento no es válido
     */
    void validateEvent(PatientEvent event);

    /**
     * Verifica si un evento es válido sin lanzar excepción
     * @param event Evento a verificar
     * @return resultado de la validación
     */
    ValidationResult isValidEvent(PatientEvent event);

    /**
     * Resultado de validación de evento
     */
    record ValidationResult(
            boolean isValid,
            String errorMessage,
            java.util.List<String> validationErrors
    ) {
        public static ValidationResult valid() {
            return new ValidationResult(true, null, java.util.List.of());
        }

        public static ValidationResult invalid(String errorMessage, java.util.List<String> errors) {
            return new ValidationResult(false, errorMessage, errors);
        }
    }
}
