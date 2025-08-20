package com.cappymerida.domain.repository;

import com.cappymerida.domain.enums.Status;
import com.cappymerida.domain.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {

    Optional<Patient> findByContactInfoEmail(String email);

    List<Patient> findByDemographicsSocialSecurityNumber(String ssn);

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.demographics.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(p.demographics.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Patient> findByNameContaining(@Param("name") String name, Pageable pageable);

    Page<Patient> findByStatus(Status status, Pageable pageable);

    boolean existsByContactInfoEmail(String email);

    boolean existsByDemographicsSocialSecurityNumber(String ssn);

    long countByStatus(Status status);

    @Query("SELECT p FROM Patient p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:searchTerm IS NULL OR " +
            "LOWER(p.demographics.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.demographics.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.contactInfo.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Patient> findPatientsWithFilters(@Param("status") Status status,
                                          @Param("searchTerm") String searchTerm,
                                          Pageable pageable);

}
