package com.cappymerida.domain.model;

import com.cappymerida.domain.enums.Relationship;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {

    @NotBlank(message = "Emergency contact name is required")
    private String name;

    @NotBlank(message = "Emergency contact phone is required")
    private String phoneNumber;

    private String email;

    @NotNull(message = "Emergency contact relationship is required")
    @Enumerated(EnumType.STRING)
    private Relationship relationship;

}
