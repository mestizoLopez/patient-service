package com.cappymerida.domain.events;

import com.cappymerida.domain.enums.EventType;
import com.cappymerida.domain.model.Patient;
import com.cappymerida.domain.records.PatientEvent;
import com.cappymerida.domain.records.PatientEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
public class EventFactory {

    public PatientEvent createPatientRegisteredEvent(Patient patient, String userId) {
        return createPatientRegisteredEvent(patient, userId, null, null);
    }

    public PatientEvent createPatientRegisteredEvent(Patient patient, String userId,
                                                     String correlationId, String causationId) {
        log.debug("Creating PatientRegistered event for patient: {}", patient.getId());

        PatientEventPayload payload = PatientEventPayload.fromPatient(
                patient, userId, "Patient registration"
        );

        Map<String, Object> metadata = Map.of(
                "source", "patients-service",
                "version", "1.0",
                "patientStatus", patient.getStatus().toString(),
                "hasEmergencyContact", patient.getEmergencyContact() != null,
                "registrationTimestamp", LocalDateTime.now().toString(),
                "demographics", Map.of(
                        "age", patient.getDemographics().getAge(),
                        "gender", patient.getDemographics().getGender().toString()
                )
        );

        if (correlationId != null && causationId != null) {
            return PatientEvent.createWithCorrelation(
                    EventType.PATIENT_REGISTERED, payload, userId, metadata, correlationId, causationId
            );
        }

        return PatientEvent.create(EventType.PATIENT_REGISTERED, payload, userId, metadata);
    }

    public PatientEvent createPatientUpdatedEvent(Patient patient, String userId, String fieldUpdated) {
        return createPatientUpdatedEvent(patient, userId, fieldUpdated, null, null);
    }

    public PatientEvent createPatientUpdatedEvent(Patient patient, String userId, String fieldUpdated,
                                                  String correlationId, String causationId) {
        log.debug("Creating PatientUpdated event for patient: {} with changes: {}",
                patient.getId(), fieldUpdated);

        PatientEventPayload payload = PatientEventPayload.fromPatient(
                patient, userId, "Patient " + fieldUpdated + " updated"
        );

        Map<String, Object> metadata = Map.of(
                "source", "patients-service",
                "version", "1.0",
                "updatedField", fieldUpdated,
                "patientStatus", patient.getStatus().toString(),
                "updateTimestamp", LocalDateTime.now().toString(),
                "previousVersion", patient.getVersion() != null ? patient.getVersion() - 1 : 0
        );

        if (correlationId != null && causationId != null) {
            return PatientEvent.createWithCorrelation(
                    EventType.PATIENT_UPDATED, payload, userId, metadata, correlationId, causationId
            );
        }

        return PatientEvent.create(EventType.PATIENT_UPDATED, payload, userId, metadata);
    }

    public PatientEvent createPatientDeactivatedEvent(Patient patient, String userId) {
        return createPatientDeactivatedEvent(patient, userId, null, null);
    }

    public PatientEvent createPatientDeactivatedEvent(Patient patient, String userId,
                                                      String correlationId, String causationId) {
        log.debug("Creating PatientDeactivated event for patient: {}", patient.getId());

        PatientEventPayload payload = PatientEventPayload.fromPatient(
                patient, userId, "Patient deactivated"
        );

        Map<String, Object> metadata = Map.of(
                "source", "patients-service",
                "version", "1.0",
                "previousStatus", "ACTIVE",
                "newStatus", "INACTIVE",
                "deactivationTimestamp", LocalDateTime.now().toString(),
                "reason", "Administrative deactivation"
        );

        if (correlationId != null && causationId != null) {
            return PatientEvent.createWithCorrelation(
                    EventType.PATIENT_DEACTIVATED, payload, userId, metadata, correlationId, causationId
            );
        }

        return PatientEvent.create(EventType.PATIENT_DEACTIVATED, payload, userId, metadata);
    }

    public PatientEvent createPatientActivatedEvent(Patient patient, String userId) {
        return createPatientActivatedEvent(patient, userId, null, null);
    }

    public PatientEvent createPatientActivatedEvent(Patient patient, String userId,
                                                    String correlationId, String causationId) {
        log.debug("Creating PatientActivated event for patient: {}", patient.getId());

        PatientEventPayload payload = PatientEventPayload.fromPatient(
                patient, userId, "Patient activated"
        );

        Map<String, Object> metadata = Map.of(
                "source", "patients-service",
                "version", "1.0",
                "previousStatus", "INACTIVE",
                "newStatus", "ACTIVE",
                "activationTimestamp", LocalDateTime.now().toString(),
                "reason", "Administrative activation"
        );

        if (correlationId != null && causationId != null) {
            return PatientEvent.createWithCorrelation(
                    EventType.PATIENT_ACTIVATED, payload, userId, metadata, correlationId, causationId
            );
        }

        return PatientEvent.create(EventType.PATIENT_ACTIVATED, payload, userId, metadata);
    }

    public PatientEvent createPatientDeletedEvent(Patient patient, String userId) {
        return createPatientDeletedEvent(patient, userId, "Administrative deletion", null, null);
    }

    public PatientEvent createPatientDeletedEvent(Patient patient, String userId, String reason,
                                                  String correlationId, String causationId) {
        log.debug("Creating PatientDeleted event for patient: {}", patient.getId());

        PatientEventPayload payload = PatientEventPayload.fromPatient(
                patient, userId, reason
        );

        Map<String, Object> metadata = Map.of(
                "source", "patients-service",
                "version", "1.0",
                "deletionReason", reason,
                "deletionTimestamp", LocalDateTime.now().toString(),
                "finalStatus", patient.getStatus().toString(),
                "irreversible", true
        );

        if (correlationId != null && causationId != null) {
            return PatientEvent.createWithCorrelation(
                    EventType.PATIENT_DELETED, payload, userId, metadata, correlationId, causationId
            );
        }

        return PatientEvent.create(EventType.PATIENT_DELETED, payload, userId, metadata);
    }

    public PatientEvent createPatientDemographicsUpdatedEvent(Patient patient, String userId) {
        log.debug("Creating PatientDemographicsUpdated event for patient: {}", patient.getId());

        PatientEventPayload payload = PatientEventPayload.fromPatient(
                patient, userId, "Patient demographics updated"
        );

        Map<String, Object> metadata = Map.of(
                "source", "patients-service",
                "version", "1.0",
                "updatedField", "demographics",
                "patientStatus", patient.getStatus().toString(),
                "updateTimestamp", LocalDateTime.now().toString(),
                "demographicsSnapshot", (Serializable) Map.of(
                        "fullName", patient.getDemographics().getFullName(),
                        "age", patient.getDemographics().getAge(),
                        "gender", patient.getDemographics().getGender().toString()
                )
        );

        return PatientEvent.create(EventType.PATIENT_DEMOGRAPHICS_UPDATED, payload, userId, metadata);
    }

    public PatientEvent createPatientContactInfoUpdatedEvent(Patient patient, String userId) {
        log.debug("Creating PatientContactInfoUpdated event for patient: {}", patient.getId());

        PatientEventPayload payload = PatientEventPayload.fromPatient(
                patient, userId, "Patient contact information updated"
        );

        Map<String, Object> metadata = Map.of(
                "source", "patients-service",
                "version", "1.0",
                "updatedField", "contactInfo",
                "patientStatus", patient.getStatus().toString(),
                "updateTimestamp", LocalDateTime.now().toString(),
                "hasEmail", patient.getContactInfo().getEmail() != null,
                "hasPhone", patient.getContactInfo().getPhoneNumber() != null
        );

        return PatientEvent.create(EventType.PATIENT_CONTACT_INFO_UPDATED, payload, userId, metadata);
    }

    public PatientEvent createPatientEmergencyContactUpdatedEvent(Patient patient, String userId) {
        log.debug("Creating PatientEmergencyContactUpdated event for patient: {}", patient.getId());

        PatientEventPayload payload = PatientEventPayload.fromPatient(
                patient, userId, "Patient emergency contact updated"
        );

        Map<String, Object> metadata = Map.of(
                "source", "patients-service",
                "version", "1.0",
                "updatedField", "emergencyContact",
                "patientStatus", patient.getStatus().toString(),
                "updateTimestamp", LocalDateTime.now().toString(),
                "emergencyContactRelationship", patient.getEmergencyContact().getRelationship().toString()
        );

        return PatientEvent.create(EventType.PATIENT_EMERGENCY_CONTACT_UPDATED, payload, userId, metadata);
    }
}

