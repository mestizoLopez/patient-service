package com.cappymerida.web.dto;

import com.cappymerida.domain.enums.Status;
import com.cappymerida.domain.model.ContactInfo;
import com.cappymerida.domain.model.Demographics;
import com.cappymerida.domain.model.EmergencyContact;
import com.cappymerida.domain.model.Patient;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PatientResponse {

    private String id;
    private Demographics demographics;
    private ContactInfo contactInfo;
    private EmergencyContact emergencyContact;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public static PatientResponse from(Patient patient) {
        PatientResponse response = new PatientResponse();
        response.setId(patient.getId());
        response.setDemographics(patient.getDemographics());
        response.setContactInfo(patient.getContactInfo());
        response.setEmergencyContact(patient.getEmergencyContact());
        response.setStatus(patient.getStatus());
        response.setCreatedAt(patient.getCreatedAt());
        response.setUpdatedAt(patient.getUpdatedAt());
        response.setVersion(patient.getVersion());
        return response;
    }

    public Patient toEntity() {
        Patient patient = new Patient();
        patient.setDemographics(this.demographics);
        patient.setContactInfo(this.contactInfo);
        patient.setEmergencyContact(this.emergencyContact);
        return patient;
    }

}
