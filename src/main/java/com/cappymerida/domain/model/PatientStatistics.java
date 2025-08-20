package com.cappymerida.domain.model;

public record PatientStatistics(
        long totalPatients,
        long activePatients,
        long inactivePatients,
        long deceasedPatients
) {}
