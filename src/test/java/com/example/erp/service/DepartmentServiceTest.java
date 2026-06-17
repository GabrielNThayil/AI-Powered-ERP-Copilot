package com.example.erp.service;

import com.example.erp.dto.department.DepartmentCreateDTO;
import com.example.erp.dto.department.DepartmentDTO;
import com.example.erp.dto.department.DepartmentUpdateDTO;
import com.example.erp.entity.Department;
import com.example.erp.entity.Employee;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.DepartmentRepository;
import com.example.erp.repository.EmployeeRepository;
import com.example.erp.mapper.DepartmentMapper;
import com.example.erp.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DepartmentService
 */
@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private DepartmentService departmentService;

    private User currentUser;
    private Employee testManager;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setName("Admin User");
        currentUser.setEmail("admin@example.com");
        currentUser.setRole(User.Role.ADMIN);
        currentUser.setStatus(User.Status.ACTIVE);

        testManager = new Employee();
        testManager.setId(UUID.randomUUID());
        testManager.setEmployeeId("MGR001");
        testManager.setUser(currentUser); // Admin is also an employee
        testManager.setName("Manager User");
        testManager.setEmail("manager@example.com");
        testManager.setPhone("1111111111");
        testManager.setDesignation("Manager");
        testManager.setSalary(80000.0);
        testManager.setDateOfJoining(LocalDateTime.now().minusYears(2));
        testManager.setEmploymentStatus("FULL_TIME");

        testDepartment = new Department();
        testDepartment.setId(UUID.randomUUID());
        testDepartment.setName("Test Department");
        testDepartment.setBudget(100000.0);
        testDepartment.setManager(testManager);
        testDepartment.setDescription("A test department");
        testDepartment.setCreatedAt(LocalDateTime.now().minusDays(1));
        testDepartment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateDepartment_Success() {
        // Arrange
        DepartmentCreateDTO dto = new DepartmentCreateDTO();
        dto.setName("New Department");
        dto.setBudget(150000.0);
        dto.setManagerId(testManager.getId());
        dto.setDescription("A new department");

        when(departmentRepository.existsByName(dto.getName())).thenReturn(false);
        when(employeeRepository.findById(dto.getManagerId())).thenReturn(Optional.of(testManager));

        Department departmentToSave = new Department();
        departmentToSave.setId(UUID.randomUUID());
        departmentToSave.setName(dto.getName());
        departmentToSave.setBudget(dto.getBudget());
        departmentToSave.setManager(testManager);
        departmentToSave.setDescription(dto.getDescription());

        when(departmentMapper.toEntity(dto)).thenReturn(departmentToSave);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Act
        DepartmentDTO result = departmentService.createDepartment(dto, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getBudget(), result.getBudget());
        verify(auditLogService).log(eq(currentUser), eq("CREATE_DEPARTMENT"), eq("DEPARTMENT"), any(), anyString());
    }

    @Test
    void testCreateDepartment_DuplicateName_ThrowsException() {
        // Arrange
        DepartmentCreateDTO dto = new DepartmentCreateDTO();
        dto.setName(testDepartment.getName()); // duplicate
        dto.setBudget(150000.0);
        dto.setManagerId(testManager.getId());
        dto.setDescription("A new department");

        when(departmentRepository.existsByName(dto.getName())).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            departmentService.createDepartment(dto, currentUser);
        });
        assertTrue(exception.getMessage().contains("Department name already exists"));
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void testGetDepartmentById_Success() {
        // Arrange
        when(departmentRepository.findById(testDepartment.getId())).thenReturn(Optional.of(testDepartment));
        when(departmentMapper.toDto(testDepartment)).thenReturn(new DepartmentDTO(
                testDepartment.getId(),
                testDepartment.getName(),
                testDepartment.getBudget(),
                testDepartment.getManager().getId(),
                testDepartment.getManager().getName(),
                testDepartment.getDescription(),
                testDepartment.getCreatedAt(),
                testDepartment.getUpdatedAt()
        ));

        // Act
        DepartmentDTO result = departmentService.getDepartmentById(testDepartment.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testDepartment.getId(), result.getId());
        assertEquals(testDepartment.getName(), result.getName());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void testUpdateDepartment_Success() {
        // Arrange
        UUID departmentId = testDepartment.getId();
        DepartmentUpdateDTO dto = new DepartmentUpdateDTO();
        dto.setName("Updated Department Name");
        dto.setBudget(120000.0);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(departmentRepository.existsByName(dto.getName())).thenReturn(false);

        Department updatedDepartment = new Department();
        updatedDepartment.setId(testDepartment.getId());
        updatedDepartment.setName(dto.getName());
        updatedDepartment.setBudget(dto.getBudget());
        updatedDepartment.setManager(testDepartment.getManager());
        updatedDepartment.setDescription(testDepartment.getDescription());
        updatedDepartment.setCreatedAt(testDepartment.getCreatedAt());
        updatedDepartment.setUpdatedAt(LocalDateTime.now());

        when(departmentRepository.save(any(Department.class))).thenReturn(updatedDepartment);

        // Act
        DepartmentDTO result = departmentService.updateDepartment(departmentId, dto, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getBudget(), result.getBudget());
        verify(auditLogService).log(eq(currentUser), eq("UPDATE_DEPARTMENT"), eq("DEPARTMENT"), eq(departmentId), anyString());
    }

    @Test
    void testGetAllDepartments_WithFilters() {
        // Arrange
        Page<Department> departmentPage = new PageImpl<>(List.of(testDepartment));
        when(departmentRepository.findAll(any(Pageable.class))).thenReturn(departmentPage);

        // Act
        Page<DepartmentDTO> result = departmentService.getAllDepartments(0, 10, "createdAt", "desc",
                testDepartment.getName(), testManager.getId(), currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testDepartment.getId(), result.getContent().get(0).getId());
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }
}