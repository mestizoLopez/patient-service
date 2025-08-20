package com.cappymerida.infrastructure.config;

import com.cappymerida.application.service.EventPublishingPatientService;
import com.cappymerida.application.service.PatientService;
import com.cappymerida.domain.events.EventFactory;
import com.cappymerida.domain.events.EventPublisher;
import com.cappymerida.domain.repository.PatientRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EventConfiguration {

    @Bean
    @ConditionalOnProperty(
            value = "kafka.event.publishing.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public PatientService eventPublishPatientService(
            PatientRepository patientRepository,
            EventPublisher eventPublisher,
            EventFactory eventFactory) {
        return new EventPublishingPatientService(patientRepository, eventPublisher, eventFactory);
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
