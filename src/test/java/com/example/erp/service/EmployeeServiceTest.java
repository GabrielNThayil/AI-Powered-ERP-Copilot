package com.example.erp.service;

import com.example.erp.dto.employee.EmployeeCreateDTO;
import com.example.erp.dto.employee.EmployeeDTO;
import com.example.erp.dto.employee.EmployeeUpdateDTO;
import com.example.erp.entity.Employee;
import com.example.erp.entity.User;
import com.example.erp.entity.Department;
import com.example.erp.exception.AppException;
import com.example.erp.repository.EmployeeRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.repository.DepartmentRepository;
import com.example.erp.mapper.EmployeeMapper;
import com.example.erp.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private EmployeeService employeeService;

    private User currentUser;
    private Department testDepartment;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setName("Test User");
        currentUser.setEmail("test@example.com");
        currentUser.setRole(User.Role.ADMIN);
        currentUser.setStatus(User.Status.ACTIVE);

        testDepartment = new Department();
        testDepartment.setId(UUID.randomUUID());
        testDepartment.setName("Test Department");
        testDepartment.setBudget(100000.0);

        testEmployee = new Employee();
        testEmployee.setId(UUID.randomUUID());
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setUser(currentUser);
        testEmployee.setName("Test Employee");
        testEmployee.setEmail("employee@example.com");
        testEmployee.setPhone("1234567890");
        testEmployee.setDesignation("Test Engineer");
        testEmployee.setSalary(50000.0);
        testEmployee.setDateOfJoining(LocalDateTime.now().minusYears(1));
        testEmployee.setDepartment(testDepartment);
        testEmployee.setEmploymentStatus("FULL_TIME");
        testEmployee.setCreatedAt(LocalDateTime.now().minusDays(1));
        testEmployee.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateEmployee_Success() {
        // Arrange
        EmployeeCreateDTO dto = new EmployeeCreateDTO();
        dto.setEmployeeId("EMP002");
        dto.setName("New Employee");
        dto.setEmail("new@example.com");
        dto.setPhone("0987654321");
        dto.setDesignation("Test Engineer");
        dto.setSalary(60000.0);
        dto.setDateOfJoining(LocalDateTime.now());
        dto.setDepartmentId(testDepartment.getId());
        dto.setEmploymentStatus("FULL_TIME");
        dto.setUserId(currentUser.getId());

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(employeeRepository.existsByEmployeeId(dto.getEmployeeId())).thenReturn(false);
        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(currentUser));
        when(departmentRepository.findById(dto.getDepartmentId())).thenReturn(Optional.of(testDepartment));
        when(employeeRepository.findByUserId(dto.getUserId())).thenReturn(Optional.empty());

        Employee employeeToSave = new Employee();
        employeeToSave.setId(UUID.randomUUID());
        employeeToSave.setEmployeeId(dto.getEmployeeId());
        employeeToSave.setUser(currentUser);
        employeeToSave.setName(dto.getName());
        employeeToSave.setEmail(dto.getEmail());
        employeeToSave.setPhone(dto.getPhone());
        employeeToSave.setDesignation(dto.getDesignation());
        testEmployee.setSalary(dto.getSalary());
        testEmployee.setDateOfJoining(dto.getDateOfJoining());
        testEmployee.setDepartment(testDepartment);
        testEmployee.setEmploymentStatus(dto.getEmploymentStatus());

        when(employeeMapper.toEntity(dto)).thenReturn(employeeToSave);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Act
        EmployeeDTO result = employeeService.createEmployee(dto, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getEmail(), result.getEmail());
        verify(auditLogService).log(eq(currentUser), eq("CREATE_EMPLOYEE"), eq("EMPLOYEE"), any(), anyString());
    }

    @Test
    void testCreateEmployee_DuplicateEmployeeId_ThrowsException() {
        // Arrange
        EmployeeCreateDTO dto = new EmployeeCreateDTO();
        dto.setEmployeeId("EMP001"); // same as testEmployee
        dto.setName("New Employee");
        dto.setEmail("new@example.com");
        dto.setPhone("0987654321");
        dto.setDesignation("Test Engineer");
        dto.setSalary(60000.0);
        dto.setDateOfJoining(LocalDateTime.now());
        dto.setDepartmentId(testDepartment.getId());
        dto.setEmploymentStatus("FULL_TIME");
        dto.setUserId(currentUser.getId());

        when(employeeRepository.existsByEmployeeId(dto.getEmployeeId())).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            employeeService.createEmployee(dto, currentUser);
        });
        assertTrue(exception.getMessage().contains("Employee ID already exists"));
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void testGetEmployeeById_Success() {
        // Arrange
        when(employeeRepository.findById(testEmployee.getId())).thenReturn(Optional.of(testEmployee));
        when(employeeMapper.toDto(testEmployee)).thenReturn(new EmployeeDTO(
                testEmployee.getId(),
                testEmployee.getEmployeeId(),
                testEmployee.getUser().getId(),
                testEmployee.getName(),
                testEmployee.getEmail(),
                testEmployee.getPhone(),
                testEmployee.getDesignation(),
                testEmployee.getSalary(),
                testEmployee.getDateOfJoining(),
                testEmployee.getDepartment().getId(),
                testEmployee.getDepartment().getName(),
                testEmployee.getEmploymentStatus(),
                testEmployee.getCreatedAt(),
                testEmployee.getUpdatedAt()
        ));

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(testEmployee.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee.getId(), result.getId());
        assertEquals(testEmployee.getName(), result.getName());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any()); // No audit log for get
    }

    @Test
    void testGetEmployeeById_NotFound_ThrowsException() {
        // Arrange
        UUID randomId = UUID.randomUUID();
        when(employeeRepository.findById(randomId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            employeeService.getEmployeeById(randomId);
        });
        assertTrue(exception.getMessage().contains("Employee not found"));
    }

    @Test
    void testUpdateEmployee_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        EmployeeUpdateDTO dto = new EmployeeUpdateDTO();
        dto.setName("Updated Name");
        dto.setSalary(55000.0);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByEmployeeId(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);

        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(testEmployee.getId());
        updatedEmployee.setEmployeeId(testEmployee.getEmployeeId());
        updatedEmployee.setUser(testEmployee.getUser());
        updatedEmployee.setName(dto.getName());
        updatedEmployee.setEmail(testEmployee.getEmail());
        updatedEmployee.setPhone(testEmployee.getPhone());
        updatedEmployee.setDesignation(testEmployee.getDesignation());
        updatedEmployee.setSalary(dto.getSalary());
        updatedEmployee.setDateOfJoining(testEmployee.getDateOfJoining());
        updatedEmployee.setDepartment(testEmployee.getDepartment());
        updatedEmployee.setEmploymentStatus(testEmployee.getEmploymentStatus());
        updatedEmployee.setCreatedAt(testEmployee.getCreatedAt());
        updatedEmployee.setUpdatedAt(LocalDateTime.now());

        when(employeeRepository.save(any(Employee.class))).thenReturn(updatedEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(employeeId, dto, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getSalary(), result.getSalary());
        verify(auditLogService).log(eq(currentUser), eq("UPDATE_EMPLOYEE"), eq("EMPLOYEE"), eq(employeeId), anyString());
    }

    @Test
    void testDeleteEmployee_Success() {
        // Arrange
        UUID employeeId = testEmployee.getId();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeRepository).delete(any(Employee.class));

        // Act
        employeeService.deleteEmployee(employeeId, currentUser);

        // Assert
        verify(employeeRepository).delete(testEmployee);
        verify(auditLogService).log(eq(currentUser), eq("DELETE_EMPLOYEE"), eq("EMPLOYEE"), eq(employeeId), anyString());
    }

    @Test
    void testGetAllEmployees_WithFilters() {
        // Arrange
        Page<Employee> employeePage = new PageImpl<>(List.of(testEmployee));
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(employeePage);

        // Act
        Page<EmployeeDTO> result = employeeService.getAllEmployees(0, 10, "createdAt", "desc",
                testEmployee.getName(), null, null, testDepartment.getId(), null, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testEmployee.getId(), result.getContent().get(0).getId());
        // Audit log is not called for getAll in service (we didn't add it)
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }
}