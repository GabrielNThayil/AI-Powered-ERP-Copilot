package com.example.erp.repository;

import com.example.erp.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Department entity
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByName(String name);

    Optional<Department> findByManagerId(UUID managerId);

    List<Department> findByManagerIdIsNotNull(); // Departments with a manager assigned
}