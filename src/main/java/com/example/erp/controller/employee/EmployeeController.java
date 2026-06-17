package com.example.erp.controller.employee;

import com.example.erp.dto.employee.EmployeeCreateDTO;
import com.example.erp.dto.employee.EmployeeDTO;
import com.example.erp.dto.employee.EmployeeUpdateDTO;
import com.example.erp.entity.User;
import com.example.erp.service.EmployeeService;
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

@Tag(name = "Employee Management", description = "Employee management APIs")
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Create a new employee
     * Only ADMIN and MANAGER can create employees
     */
    @Operation(summary = "Create a new employee", description = "Create a new employee record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee created",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeCreateDTO dto,
                                                      @AuthenticationCurrentUser User currentUser) {
        log.info("Creating employee with employeeId: {} by user: {}", dto.getEmployeeId(), currentUser.getEmail());
        EmployeeDTO createdEmployee = employeeService.createEmployee(dto, currentUser);
        return ResponseEntity.status(201).body(createdEmployee);
    }

    /**
     * Get employee by ID
     */
    @Operation(summary = "Get employee by ID", description = "Get employee details by internal ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee found",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable UUID id,
                                                       @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching employee with ID: {} by user: {}", id, currentUser.getEmail());
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    /**
     * Get employee by employee ID (like EMP0001)
     */
    @Operation(summary = "Get employee by employee ID", description = "Get employee details by employee ID (e.g., EMP0001)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee found",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/employee-id/{employeeId}")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmployeeId(@PathVariable String employeeId,
                                                               @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching employee with employeeId: {} by user: {}", employeeId, currentUser.getEmail());
        EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(employeeId);
        return ResponseEntity.ok(employee);
    }

    /**
     * Get all employees with pagination, sorting, and filtering
     * Only ADMIN and MANAGER can view all employees; EMPLOYEE can only view their own?
     * But we'll implement filtering by the service. For now, we'll allow all authenticated users to view with filters.
     * We'll add security later with @PreAuthorize.
     */
    @Operation(summary = "Get all employees", description = "Get all employees with pagination, sorting, and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of employees"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) String employmentStatus,
            @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching employees with filters by user: {}", currentUser.getEmail());
        Page<EmployeeDTO> employees = employeeService.getAllEmployees(page, size, sortBy, direction,
                name, email, designation, departmentId, employmentStatus);
        return ResponseEntity.ok(employees);
    }

    /**
     * Update an employee
     * Only ADMIN and MANAGER can update employees
     */
    @Operation(summary = "Update an employee", description = "Update an existing employee record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee updated",
                    content = @Content(schema = @Schema(implementation = EmployeeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable UUID id,
                                                      @Valid @RequestBody EmployeeUpdateDTO dto,
                                                      @AuthenticationCurrentUser User currentUser) {
        log.info("Updating employee with ID: {} by user: {}", id, currentUser.getEmail());
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, dto, currentUser);
        return ResponseEntity.ok(updatedEmployee);
    }

    /**
     * Delete an employee
     * Only ADMIN can delete employees
     */
    @Operation(summary = "Delete an employee", description = "Delete an employee record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Employee deleted"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id,
                                               @AuthenticationCurrentUser User currentUser) {
        log.info("Deleting employee with ID: {} by user: {}", id, currentUser.getEmail());
        employeeService.deleteEmployee(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get employees by department ID
     */
    @Operation(summary = "Get employees by department ID", description = "Get all employees belonging to a specific department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of employees in the department"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<java.util.List<EmployeeDTO>> getEmployeesByDepartmentId(@PathVariable UUID departmentId,
                                                                                  @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching employees for department ID: {} by user: {}", departmentId, currentUser.getEmail());
        java.util.List<EmployeeDTO> employees = employeeService.getEmployeesByDepartmentId(departmentId);
        return ResponseEntity.ok(employees);
    }
}