package com.cappymerida.application.service;

import com.cappymerida.domain.enums.Status;
import com.cappymerida.domain.events.EventPublisher;
import com.cappymerida.domain.model.Patient;
import com.cappymerida.domain.model.PatientStatistics;
import com.cappymerida.domain.repository.PatientRepository;
import com.cappymerida.domain.events.EventFactory;
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
@Slf4j
@Transactional
@Qualifier("patientServiceEventIntegration")
@ConditionalOnProperty(value = "kafka.event.publishing.enabled", havingValue = "true", matchIfMissing = true)
public class PatientServiceEventIntegration extends PatientService {

    private final EventPublisher eventPublisher;
    private final EventFactory eventFactory;

    public PatientServiceEventIntegration(PatientRepository patientRepository,
                                          EventPublisher eventPublisher,
                                          EventFactory eventFactory) {
        super(patientRepository);
        this.eventPublisher = eventPublisher;
        this.eventFactory = eventFactory;
    }

    @Override
    public Patient createPatient(Patient patient) {
        log.info("Creating new patient with events: {}", patient.getDemographics().getFullName());

        // Validaciones de negocio (usa métodos protegidos de la clase padre)
        validateUniqueConstraints(patient);

        // Guardar paciente
        Patient savedPatient = getPatientRepository().save(patient);

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

        Patient existingPatient = getPatientRepository().findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Detectar qué campos cambiaron antes de la actualización
        String changedFields = detectChangedFields(existingPatient, updatedPatient);

        // Validar unicidad excluyendo el paciente actual
        validateUniqueConstraintsForUpdate(id, updatedPatient);

        // Actualizar campos
        existingPatient.setDemographics(updatedPatient.getDemographics());
        existingPatient.setContactInfo(updatedPatient.getContactInfo());
        existingPatient.setEmergencyContact(updatedPatient.getEmergencyContact());

        Patient savedPatient = getPatientRepository().save(existingPatient);

        // Publicar eventos específicos según los campos actualizados
        String userId = getCurrentUserId();
        publishSpecificUpdateEvents(savedPatient, userId, changedFields);

        log.info("Patient updated: {} with changes: {}", savedPatient.getId(), changedFields);
        return savedPatient;
    }

    @Override
    public void deactivatePatient(String id) {
        log.info("Deactivating patient with events: {}", id);

        Patient patient = getPatientRepository().findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        patient.deactivate();
        getPatientRepository().save(patient);

        // Publicar evento
        String userId = getCurrentUserId();
        var event = eventFactory.createPatientDeactivatedEvent(patient, userId);

        eventPublisher.publishPatientEventAsync(event);

        log.info("Patient deactivated: {}", id);
    }

    @Override
    public void activatePatient(String id) {
        log.info("Activating patient with events: {}", id);

        Patient patient = getPatientRepository().findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        patient.activate();
        getPatientRepository().save(patient);

        // Publicar evento
        String userId = getCurrentUserId();
        var event = eventFactory.createPatientActivatedEvent(patient, userId);

        eventPublisher.publishPatientEventAsync(event);

        log.info("Patient activated: {}", id);
    }

    @Override
    public void deletePatient(String id) {
        log.info("Deleting patient with events: {}", id);

        Patient patient = getPatientRepository().findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Publicar evento antes de eliminar
        String userId = getCurrentUserId();
        var event = eventFactory.createPatientDeletedEvent(patient, userId);

        eventPublisher.publishPatientEventAsync(event);

        getPatientRepository().deleteById(id);
        log.info("Patient deleted: {}", id);
    }

    // Métodos de solo lectura no publican eventos - delegan a la clase padre
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

    // Métodos auxiliares privados
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private void publishSpecificUpdateEvents(Patient patient, String userId, String changedFields) {
        // Publicar evento general de actualización
        var generalUpdateEvent = eventFactory.createPatientUpdatedEvent(patient, userId, changedFields);
        eventPublisher.publishPatientEventAsync(generalUpdateEvent);

        // Publicar eventos específicos según los campos actualizados
        if (changedFields.contains("demographics")) {
            var demographicsEvent = eventFactory.createPatientDemographicsUpdatedEvent(patient, userId);
            eventPublisher.publishPatientEventAsync(demographicsEvent);
        }

        if (changedFields.contains("contactInfo")) {
            var contactInfoEvent = eventFactory.createPatientContactInfoUpdatedEvent(patient, userId);
            eventPublisher.publishPatientEventAsync(contactInfoEvent);
        }

        if (changedFields.contains("emergencyContact")) {
            var emergencyContactEvent = eventFactory.createPatientEmergencyContactUpdatedEvent(patient, userId);
            eventPublisher.publishPatientEventAsync(emergencyContactEvent);
        }
    }
}
