package com.cappymerida.web.dto;

import com.cappymerida.domain.model.ContactInfo;
import com.cappymerida.domain.model.Demographics;
import com.cappymerida.domain.model.EmergencyContact;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PatientRequest {

    @NotNull(message = "Demographics are required")
    @Valid
    private Demographics demographics;

    @Valid
    private ContactInfo contactInfo;

    @Valid
    private EmergencyContact emergencyContact;

}
