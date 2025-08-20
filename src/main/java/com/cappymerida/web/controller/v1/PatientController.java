package com.cappymerida.web.controller.v1;

import com.cappymerida.application.service.PatientService;
import com.cappymerida.domain.enums.Status;
import com.cappymerida.domain.model.Patient;
import com.cappymerida.domain.model.PatientStatistics;
import com.cappymerida.web.dto.PatientRequest;
import com.cappymerida.web.dto.PatientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patient Management", description = "APIs for managing patient information")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @Operation(summary = "Create new patient", description = "Register a new patient in the system")
    @ApiResponse(responseCode = "201", description = "Patient created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid patient data")
    @PreAuthorize("hasRole('ROLE_DOCTOR') or hasRole('ROLE_NURSE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        log.info("Creating new patient: {}", request.getDemographics().getFullName());

        Patient patient = new Patient();
        patient.setDemographics(request.getDemographics());
        patient.setContactInfo(request.getContactInfo());
        patient.setEmergencyContact(request.getEmergencyContact());

        Patient createdPatient = patientService.createPatient(patient);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PatientResponse.from(createdPatient));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID", description = "Retrieve patient information by ID")
    @ApiResponse(responseCode = "200", description = "Patient found")
    @ApiResponse(responseCode = "404", description = "Patient not found")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable String id) {
        log.debug("Getting patient by ID: {}", id);

        return patientService.findPatientById(id)
                .map(patient -> ResponseEntity.ok(PatientResponse.from(patient)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Search patients", description = "Search patients with optional filters")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<Page<PatientResponse>> searchPatients(
            @Parameter(description = "Search term (name or email)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Patient status filter")
            @RequestParam(required = false) Status status,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("Searching patients with term: {}, status: {}", search, status);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Patient> patients = patientService.searchPatients(search, status, pageable);
        Page<PatientResponse> response = patients.map(PatientResponse::from);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient", description = "Update existing patient information")
    @ApiResponse(responseCode = "200", description = "Patient updated successfully")
    @ApiResponse(responseCode = "404", description = "Patient not found")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable String id,
            @Valid @RequestBody PatientRequest request) {

        log.info("Updating patient with ID: {}", id);

        Patient patient = new Patient();
        patient.setDemographics(request.getDemographics());
        patient.setContactInfo(request.getContactInfo());
        patient.setEmergencyContact(request.getEmergencyContact());

        try {
            Patient updatedPatient = patientService.updatePatient(id, patient);
            return ResponseEntity.ok(PatientResponse.from(updatedPatient));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate patient", description = "Mark patient as inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivatePatient(@PathVariable String id) {
        log.info("Deactivating patient with ID: {}", id);

        try {
            patientService.deactivatePatient(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate patient", description = "Mark patient as active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activatePatient(@PathVariable String id) {
        log.info("Activating patient with ID: {}", id);

        try {
            patientService.activatePatient(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete patient", description = "Permanently delete patient record")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable String id) {
        log.info("Deleting patient with ID: {}", id);

        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get patient statistics", description = "Retrieve patient count statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientStatistics> getStatistics() {
        log.debug("Getting patient statistics");

        PatientStatistics stats = patientService.getStatistics();
        return ResponseEntity.ok(stats);
    }

}
