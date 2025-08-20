package com.cappymerida.domain.records;

import com.cappymerida.domain.enums.Status;
import com.cappymerida.domain.model.ContactInfo;
import com.cappymerida.domain.model.Demographics;
import com.cappymerida.domain.model.EmergencyContact;
import com.cappymerida.domain.model.Patient;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record PatientEventPayload(
        String patientId,
        Demographics demographics,
        ContactInfo contactInfo,
        EmergencyContact emergencyContact,
        Status status,
        String changedBy,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        String changeReason,
        Long patientVersion
) {

    /**
     * Factory method para crear payload desde una entidad Patient
     */
    public static PatientEventPayload fromPatient(Patient patient, String changedBy, String changeReason) {
        return new PatientEventPayload(
                patient.getId(),
                patient.getDemographics(),
                patient.getContactInfo(),
                patient.getEmergencyContact(),
                patient.getStatus(),
                changedBy,
                LocalDateTime.now(),
                changeReason,
                patient.getVersion()
        );
    }

    /**
     * Factory method para crear payload con timestamp específico
     */
    public static PatientEventPayload fromPatientWithTimestamp(Patient patient, String changedBy,
                                                               String changeReason, LocalDateTime timestamp) {
        return new PatientEventPayload(
                patient.getId(),
                patient.getDemographics(),
                patient.getContactInfo(),
                patient.getEmergencyContact(),
                patient.getStatus(),
                changedBy,
                timestamp,
                changeReason,
                patient.getVersion()
        );
    }

    /**
     * Crea una copia del payload con un nuevo motivo de cambio
     */
    public PatientEventPayload withChangeReason(String newChangeReason) {
        return new PatientEventPayload(
                this.patientId,
                this.demographics,
                this.contactInfo,
                this.emergencyContact,
                this.status,
                this.changedBy,
                this.timestamp,
                newChangeReason,
                this.patientVersion
        );
    }

    /**
     * Verifica si el payload contiene información de contacto de emergencia
     */
    public boolean hasEmergencyContact() {
        return this.emergencyContact != null;
    }

    /**
     * Verifica si el payload contiene email
     */
    public boolean hasEmail() {
        return this.contactInfo != null &&
                this.contactInfo.getEmail() != null &&
                !this.contactInfo.getEmail().isBlank();
    }

    /**
     * Obtiene el nombre completo del paciente
     */
    public String getPatientFullName() {
        return this.demographics != null ? this.demographics.getFullName() : "Unknown";
    }
}
