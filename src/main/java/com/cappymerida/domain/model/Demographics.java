package com.cappymerida.domain.model;

import com.cappymerida.domain.enums.Gender;
import com.cappymerida.domain.enums.MaritalStatus;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Demographics {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String middleName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String socialSecurityNumber;
    private String preferredLanguage;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    public String getFullName() {
        return middleName != null && !middleName.isBlank()
                ? firstName + " " + middleName + " " + lastName
                : firstName + " " + lastName;
    }

    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

}
