package com.example.erp.repository;

import com.example.erp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Employee entity
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartmentId(UUID departmentId);

    List<Employee> findByEmploymentStatus(String status);

    Optional<Employee> findByUserId(UUID userId);

    @Query("SELECT e.department.id, e.department.name, COUNT(e) FROM Employee e WHERE e.department IS NOT NULL GROUP BY e.department.id, e.department.name")
    List<Object[]> countEmployeesPerDepartment();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.dateOfJoining >= :startDate")
    Long countNewEmployeesSince(@Param("startDate") LocalDateTime startDate);
}