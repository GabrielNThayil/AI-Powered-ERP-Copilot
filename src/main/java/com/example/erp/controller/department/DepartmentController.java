package com.example.erp.controller.department;

import com.example.erp.dto.department.DepartmentCreateDTO;
import com.example.erp.dto.department.DepartmentDTO;
import com.example.erp.dto.department.DepartmentUpdateDTO;
import com.example.erp.dto.employee.EmployeeDTO;
import com.example.erp.entity.User;
import com.example.erp.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationCurrentUser;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Department Management", description = "Department management APIs")
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * Create a new department
     * Only ADMIN and MANAGER can create departments
     */
    @Operation(summary = "Create a new department", description = "Create a new department record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Department created",
                    content = @Content(schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody DepartmentCreateDTO dto,
                                                          @AuthenticationCurrentUser User currentUser) {
        log.info("Creating department with name: {} by user: {}", dto.getName(), currentUser.getEmail());
        DepartmentDTO createdDepartment = departmentService.createDepartment(dto, currentUser);
        return ResponseEntity.status(201).body(createdDepartment);
    }

    /**
     * Get department by ID
     */
    @Operation(summary = "Get department by ID", description = "Get department details by internal ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department found",
                    content = @Content(schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable UUID id,
                                                           @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching department with ID: {} by user: {}", id, currentUser.getEmail());
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    /**
     * Get department by name
     */
    @Operation(summary = "Get department by name", description = "Get department details by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department found",
                    content = @Content(schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<DepartmentDTO> getDepartmentByName(@PathVariable String name,
                                                             @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching department with name: {} by user: {}", name, currentUser.getEmail());
        DepartmentDTO department = departmentService.getDepartmentByName(name);
        return ResponseEntity.ok(department);
    }

    /**
     * Get all departments with pagination, sorting, and filtering
     */
    @Operation(summary = "Get all departments", description = "Get all departments with pagination, sorting, and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of departments"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping
    public ResponseEntity<Page<DepartmentDTO>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID managerId,
            @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching departments with filters by user: {}", currentUser.getEmail());
        Page<DepartmentDTO> departments = departmentService.getAllDepartments(page, size, sortBy, direction,
                name, managerId);
        return ResponseEntity.ok(departments);
    }

    /**
     * Update a department
     * Only ADMIN and MANAGER can update departments
     */
    @Operation(summary = "Update a department", description = "Update an existing department record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department updated",
                    content = @Content(schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(@PathVariable UUID id,
                                                          @Valid @RequestBody DepartmentUpdateDTO dto,
                                                          @AuthenticationCurrentUser User currentUser) {
        log.info("Updating department with ID: {} by user: {}", id, currentUser.getEmail());
        DepartmentDTO updatedDepartment = departmentService.updateDepartment(id, dto, currentUser);
        return ResponseEntity.ok(updatedDepartment);
    }

    /**
     * Delete a department
     * Only ADMIN can delete departments
     */
    @Operation(summary = "Delete a department", description = "Delete a department record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Department deleted"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id,
                                                 @AuthenticationCurrentUser User currentUser) {
        log.info("Deleting department with ID: {} by user: {}", id, currentUser.getEmail());
        departmentService.deleteDepartment(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get employees in a department
     */
    @Operation(summary = "Get employees in a department", description = "Get all employees belonging to a specific department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of employees in the department"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/{departmentId}/employees")
    public ResponseEntity<java.util.List<EmployeeDTO>> getEmployeesInDepartment(@PathVariable UUID departmentId,
                                                                                @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching employees for department ID: {} by user: {}", departmentId, currentUser.getEmail());
        java.util.List<EmployeeDTO> employees = departmentService.getEmployeesInDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }
}