package com.cappymerida.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;
    private String alternatePhoneNumber;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

}
