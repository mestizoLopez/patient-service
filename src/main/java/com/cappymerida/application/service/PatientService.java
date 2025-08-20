package com.cappymerida.application.service;

import com.cappymerida.domain.enums.Status;
import com.cappymerida.domain.model.Patient;
import com.cappymerida.domain.model.PatientStatistics;
import com.cappymerida.domain.repository.PatientRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Qualifier("patientService")
@Transactional
public class PatientService {

    private PatientRepository patientRepository;

    public Patient createPatient(Patient patient) {
        log.info("Creating new patient: {}", patient.getDemographics().getFullName());

        // Validaciones de unicidad
        validateUniqueConstraints(patient);

        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient created with ID: {}", savedPatient.getId());
        return savedPatient;
    }

    @Transactional(readOnly = true)
    public Optional<Patient> findPatientById(String id) {
        log.debug("Finding patient by ID: {}", id);
        return patientRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Patient> findPatientByEmail(String email) {
        log.debug("Finding patient by email: {}", email);
        return patientRepository.findByContactInfoEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<Patient> findAllPatients(Pageable pageable) {
        log.debug("Finding all patients with pagination");
        return patientRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Patient> searchPatients(String searchTerm, Status status, Pageable pageable) {
        log.debug("Searching patients with term: {}, status: {}", searchTerm, status);
        return patientRepository.findPatientsWithFilters(status, searchTerm, pageable);
    }

    public Patient updatePatient(String id, Patient updatedPatient) {
        log.info("Updating patient with ID: {}", id);

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Validar unicidad excluyendo el paciente actual
        validateUniqueConstraintsForUpdate(id, updatedPatient);

        // Actualizar campos
        existingPatient.setDemographics(updatedPatient.getDemographics());
        existingPatient.setContactInfo(updatedPatient.getContactInfo());
        existingPatient.setEmergencyContact(updatedPatient.getEmergencyContact());

        Patient savedPatient = patientRepository.save(existingPatient);
        log.info("Patient updated: {}", savedPatient.getId());
        return savedPatient;
    }

    public void deactivatePatient(String id) {
        log.info("Deactivating patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        patient.deactivate();
        patientRepository.save(patient);
        log.info("Patient deactivated: {}", id);
    }

    public void activatePatient(String id) {
        log.info("Activating patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        patient.activate();
        patientRepository.save(patient);
        log.info("Patient activated: {}", id);
    }

    public void deletePatient(String id) {
        log.info("Deleting patient with ID: {}", id);

        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient not found");
        }

        patientRepository.deleteById(id);
        log.info("Patient deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public PatientStatistics getStatistics() {
        log.debug("Calculating patient statistics");

        long totalPatients = patientRepository.count();
        long activePatients = patientRepository.countByStatus(Status.ACTIVE);
        long inactivePatients = patientRepository.countByStatus(Status.INACTIVE);
        long deceasedPatients = patientRepository.countByStatus(Status.DECEASED);

        return new PatientStatistics(totalPatients, activePatients, inactivePatients, deceasedPatients);
    }

    // Métodos de validación protegidos para uso en subclases
    protected void validateUniqueConstraints(Patient patient) {
        // Validar email único
        if (patient.getContactInfo().getEmail() != null &&
                patientRepository.existsByContactInfoEmail(patient.getContactInfo().getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Validar SSN único
        if (patient.getDemographics().getSocialSecurityNumber() != null &&
                patientRepository.existsByDemographicsSocialSecurityNumber(
                        patient.getDemographics().getSocialSecurityNumber())) {
            throw new IllegalArgumentException("Social Security Number already exists");
        }
    }

    protected void validateUniqueConstraintsForUpdate(String excludePatientId, Patient updatedPatient) {
        // Validar email único (excluyendo el paciente actual)
        if (updatedPatient.getContactInfo().getEmail() != null) {
            var existingByEmail = patientRepository.findByContactInfoEmail(updatedPatient.getContactInfo().getEmail());
            if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(excludePatientId)) {
                throw new IllegalArgumentException("Email already exists for another patient");
            }
        }

        // Validar SSN único (excluyendo el paciente actual)
        if (updatedPatient.getDemographics().getSocialSecurityNumber() != null) {
            var existingBySsn = patientRepository.findByDemographicsSocialSecurityNumber(
                    updatedPatient.getDemographics().getSocialSecurityNumber());
            if (!existingBySsn.isEmpty() &&
                    existingBySsn.stream().anyMatch(p -> !p.getId().equals(excludePatientId))) {
                throw new IllegalArgumentException("Social Security Number already exists for another patient");
            }
        }
    }

    protected String detectChangedFields(Patient existing, Patient updated) {
        java.util.List<String> changes = new java.util.ArrayList<>();

        if (!existing.getDemographics().equals(updated.getDemographics())) {
            changes.add("demographics");
        }
        if (!existing.getContactInfo().equals(updated.getContactInfo())) {
            changes.add("contactInfo");
        }
        if (!existing.getEmergencyContact().equals(updated.getEmergencyContact())) {
            changes.add("emergencyContact");
        }

        return String.join(",", changes);
    }

    // Getter protegido para el repository
    protected PatientRepository getPatientRepository() {
        return patientRepository;
    }
}