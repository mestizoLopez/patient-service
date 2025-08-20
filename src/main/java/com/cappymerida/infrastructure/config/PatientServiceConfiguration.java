package com.cappymerida.infrastructure.config;

import com.cappymerida.application.service.PatientService;
import com.cappymerida.application.service.PatientServiceEventIntegration;
import com.cappymerida.domain.events.EventPublisher;
import com.cappymerida.domain.repository.PatientRepository;
import com.cappymerida.domain.events.EventFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PatientServiceConfiguration {

    @Bean
    @ConditionalOnProperty(
            value = "kafka.event.publishing.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public PatientService eventEnabledPatientService(
            PatientRepository patientRepository,
            EventPublisher eventPublisher,
            EventFactory eventFactory) {
        return new PatientServiceEventIntegration(patientRepository, eventPublisher, eventFactory);
    }

    @Bean
    @ConditionalOnProperty(
            value = "kafka.event.publishing.enabled",
            havingValue = "false"
    )
    public PatientService basicPatientService(PatientRepository patientRepository) {
        return new PatientService(patientRepository);
    }
}
