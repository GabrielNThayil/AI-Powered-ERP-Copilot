package com.example.erp.service;

import com.example.erp.dto.department.DepartmentCreateDTO;
import com.example.erp.dto.department.DepartmentDTO;
import com.example.erp.dto.department.DepartmentUpdateDTO;
import com.example.erp.entity.Department;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.DepartmentRepository;
import com.example.erp.repository.EmployeeRepository;
import com.example.erp.mapper.DepartmentMapper;
import com.example.erp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Department management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;
    private final AuditLogService auditLogService;

    /**
     * Create a new department
     */
    public DepartmentDTO createDepartment(DepartmentCreateDTO dto, User currentUser) {
        // Check if department name already exists
        if (departmentRepository.existsByName(dto.getName())) {
            throw new AppException("Department name already exists: " + dto.getName());
        }

        // Check if the manager exists
        Employee manager = employeeRepository.findById(dto.getManagerId())
                .orElseThrow(() -> new AppException("Manager not found with ID: " + dto.getManagerId()));

        // Create department entity
        Department department = departmentMapper.toEntity(dto);
        department.setManager(manager);

        // Save department
        Department savedDepartment = departmentRepository.save(department);

        // Log the creation
        auditLogService.log(currentUser, "CREATE_DEPARTMENT", "DEPARTMENT", savedDepartment.getId(),
                "Created department with name: " + savedDepartment.getName());

        // Convert to DTO
        return departmentMapper.toDto(savedDepartment);
    }

    /**
     * Get department by ID
     */
    public DepartmentDTO getDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Department not found with ID: " + id));
        return departmentMapper.toDto(department);
    }

    /**
     * Get department by name
     */
    public DepartmentDTO getDepartmentByName(String name) {
        Department department = departmentRepository.findByName(name)
                .orElseThrow(() -> new AppException("Department not found with name: " + name));
        return departmentMapper.toDto(department);
    }

    /**
     * Get all departments with pagination, sorting, and filtering
     */
    public Page<DepartmentDTO> getAllDepartments(int page, int size, String sortBy, String direction,
                                                 String name, UUID managerId) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // We'll fetch all and filter for simplicity (not efficient for large data)
        Page<Department> departmentsPage = departmentRepository.findAll(pageable);

        // Apply filters
        List<Department> filteredList = departmentsPage.getContent().stream()
                .filter(dept -> name == null ||
                        dept.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(dept -> managerId == null ||
                        (dept.getManager() != null && dept.getManager().getId().equals(managerId)))
                .collect(Collectors.toList());

        // Create a new page with filtered content
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        List<Department> filteredPageContent = filteredList.subList(start, end);

        return new PageImpl<>(filteredPageContent, pageable, filteredList.size())
                .map(departmentMapper::toDto);
    }

    /**
     * Update a department
     */
    public DepartmentDTO updateDepartment(UUID id, DepartmentUpdateDTO dto, User currentUser) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Department not found with ID: " + id));

        // Check if department name is being changed and if it already exists
        if (dto.getName() != null && !dto.getName().equals(department.getName())) {
            if (departmentRepository.existsByName(dto.getName())) {
                throw new AppException("Department name already exists: " + dto.getName());
            }
        }

        // Update fields if they are not null
        if (dto.getName() != null) {
            department.setName(dto.getName());
        }
        if (dto.getBudget() != null) {
            department.setBudget(dto.getBudget());
        }
        if (dto.getManagerId() != null) {
            Employee manager = employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new AppException("Manager not found with ID: " + dto.getManagerId()));
            department.setManager(manager);
        }
        if (dto.getDescription() != null) {
            department.setDescription(dto.getDescription());
        }

        Department updatedDepartment = departmentRepository.save(department);

        // Log the update
        auditLogService.log(currentUser, "UPDATE_DEPARTMENT", "DEPARTMENT", updatedDepartment.getId(),
                "Updated department with name: " + updatedDepartment.getName());

        return departmentMapper.toDto(updatedDepartment);
    }

    /**
     * Delete a department
     */
    public void deleteDepartment(UUID id, User currentUser) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException("Department not found with ID: " + id));

        // Check if department has employees
        if (!departmentRepository.findById(id).get().getEmployees().isEmpty()) {
            throw new AppException("Cannot delete department because it has employees assigned");
        }

        // Log before deletion
        auditLogService.log(currentUser, "DELETE_DEPARTMENT", "DEPARTMENT", department.getId(),
                "Deleted department with name: " + department.getName());

        departmentRepository.delete(department);
    }

    /**
     * Get employees in a department
     */
    public java.util.List<com.example.erp.dto.employee.EmployeeDTO> getEmployeesInDepartment(UUID departmentId) {
        // We can use the employee service or repository directly
        // For now, we'll use the employee repository
        java.util.List<com.example.erp.entity.Employee> employees = employeeRepository.findByDepartmentId(departmentId);
        return employees.stream()
                .map(com.example.erp.mapper.EmployeeMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }
}