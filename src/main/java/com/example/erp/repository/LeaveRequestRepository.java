package com.example.erp.repository;

import com.example.erp.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for LeaveRequest entity
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByEmployeeId(UUID employeeId);

    List<LeaveRequest> findByStatus(String status);

    List<LeaveRequest> findByStartDateBetween(LocalDateTime start, LocalDateTime end);

    Optional<LeaveRequest> findByIdAndEmployeeId(UUID id, UUID employeeId);

    Long countByStatus(String status);
}