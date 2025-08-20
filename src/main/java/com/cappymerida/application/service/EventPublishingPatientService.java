package com.cappymerida.application.service;

import com.cappymerida.domain.enums.Status;
import com.cappymerida.domain.events.EventFactory;
import com.cappymerida.domain.model.PatientStatistics;
import com.cappymerida.domain.events.EventPublisher;
import com.cappymerida.domain.model.Patient;
import com.cappymerida.domain.repository.PatientRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Transactional
@Qualifier("eventPublishingPatientService")
@ConditionalOnProperty(value = "kafka.event.publishing.enabled", havingValue = "true", matchIfMissing = true)
public class EventPublishingPatientService extends PatientService {

    private PatientRepository patientRepository;
    private EventPublisher eventPublisher;
    private EventFactory eventFactory;

    @Override
    public Patient createPatient(Patient patient) {
        log.info("Creating new patient with events: {}", patient.getDemographics().getFullName());

        // Validaciones de negocio (heredadas del servicio base)
        super.validateUniqueConstraints(patient);

        // Guardar paciente
        Patient savedPatient = patientRepository.save(patient);

        // Publicar evento
        String userId = getCurrentUserId();
        var event = eventFactory.createPatientRegisteredEvent(savedPatient, userId);

        eventPublisher.publishPatientEventAsync(event);

        log.info("Patient created with ID: {} and event published", savedPatient.getId());
        return savedPatient;
    }

    @Override
    public Patient updatePatient(String id, Patient updatedPatient) {
        log.info("Updating patient with events: {}", id);

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Validar unicidad excluyendo el paciente actual
        super.validateUniqueConstraintsForUpdate(id, updatedPatient);

        // Detectar qué campos cambiaron
        String changedFields = detectChangedFields(existingPatient, updatedPatient);

        // Actualizar campos
        existingPatient.setDemographics(updatedPatient.getDemographics());
        existingPatient.setContactInfo(updatedPatient.getContactInfo());
        existingPatient.setEmergencyContact(updatedPatient.getEmergencyContact());

        Patient savedPatient = patientRepository.save(existingPatient);

        // Publicar evento
        String userId = getCurrentUserId();
        var event = eventFactory.createPatientUpdatedEvent(savedPatient, userId, changedFields);

        eventPublisher.publishPatientEventAsync(event);

        log.info("Patient updated: {} with changes: {}", savedPatient.getId(), changedFields);
        return savedPatient;
    }

    @Override
    public void deactivatePatient(String id) {
        log.info("Deactivating patient with events: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        patient.deactivate();
        patientRepository.save(patient);

        // Publicar evento
        String userId = getCurrentUserId();
        var event = eventFactory.createPatientDeactivatedEvent(patient, userId);

        eventPublisher.publishPatientEventAsync(event);

        log.info("Patient deactivated: {}", id);
    }

    @Override
    public void activatePatient(String id) {
        log.info("Activating patient with events: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        patient.activate();
        patientRepository.save(patient);

        // Publicar evento
        String userId = getCurrentUserId();
        var event = eventFactory.createPatientActivatedEvent(patient, userId);

        eventPublisher.publishPatientEventAsync(event);

        log.info("Patient activated: {}", id);
    }

    @Override
    public void deletePatient(String id) {
        log.info("Deleting patient with events: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Publicar evento antes de eliminar
        String userId = getCurrentUserId();
        var event = eventFactory.createPatientDeletedEvent(patient, userId);

        eventPublisher.publishPatientEventAsync(event);

        patientRepository.deleteById(id);
        log.info("Patient deleted: {}", id);
    }

    // Métodos de solo lectura no publican eventos
    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> findPatientById(String id) {
        return super.findPatientById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> findPatientByEmail(String email) {
        return super.findPatientByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Patient> findAllPatients(Pageable pageable) {
        return super.findAllPatients(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Patient> searchPatients(String searchTerm, Status status, Pageable pageable) {
        return super.searchPatients(searchTerm, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientStatistics getStatistics() {
        return super.getStatistics();
    }

    // Métodos auxiliares
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    protected String detectChangedFields(Patient existing, Patient updated) {
        StringBuilder changes = new StringBuilder();

        if (!existing.getDemographics().equals(updated.getDemographics())) {
            changes.append("demographics,");
        }
        if (!existing.getContactInfo().equals(updated.getContactInfo())) {
            changes.append("contactInfo,");
        }
        if (!existing.getEmergencyContact().equals(updated.getEmergencyContact())) {
            changes.append("emergencyContact,");
        }

        String result = changes.toString();
        return result.endsWith(",") ? result.substring(0, result.length() - 1) : result;
    }
}
