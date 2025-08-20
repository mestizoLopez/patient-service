package com.cappymerida.domain.model;

import com.cappymerida.domain.enums.Status;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patients")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class Patient {

    @Id
    private String id;

    @NotNull
    @Valid
    @Embedded
    private Demographics demographics;

    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "email", column = @Column(name = "contact_email")),
            @AttributeOverride(name = "phoneNumber", column = @Column(name = "contact_phone")),
            @AttributeOverride(name = "alternatePhoneNumber", column = @Column(name = "contact_alt_phone")),
            @AttributeOverride(name = "street", column = @Column(name = "address_street")),
            @AttributeOverride(name = "city", column = @Column(name = "address_city")),
            @AttributeOverride(name = "state", column = @Column(name = "address_state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "address_zip")),
            @AttributeOverride(name = "country", column = @Column(name = "address_country"))
    })
    private ContactInfo contactInfo;

    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "emergency_name")),
            @AttributeOverride(name = "phoneNumber", column = @Column(name = "emergency_phone")),
            @AttributeOverride(name = "email", column = @Column(name = "emergency_email")),
            @AttributeOverride(name = "relationship", column = @Column(name = "emergency_relationship"))
    })
    private EmergencyContact emergencyContact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    private void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public void activate() {
        this.status = Status.ACTIVE;
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
    }

}
