package com.example.erp.service;

import com.example.erp.dto.expense.ExpenseApprovalDTO;
import com.example.erp.dto.expense.ExpenseCreateDTO;
import com.example.erp.dto.expense.ExpenseDTO;
import com.example.erp.entity.Expense;
import com.example.erp.entity.Employee;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.ExpenseRepository;
import com.example.erp.repository.EmployeeRepository;
import com.example.erp.mapper.ExpenseMapper;
import com.example.erp.service.AuditLogService;
import org.junit.jupiter.BeforeEach;
import org.junit.jupiter.Test;
import org.junit.jupiter.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExpenseService
 */
@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ExpenseService expenseService;

    private User currentUser;
    private Employee testEmployee;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setName("Test User");
        currentUser.setEmail("test@example.com");
        currentUser.setRole(User.Role.EMPLOYEE);
        currentUser.setStatus(User.Status.ACTIVE);

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
        testEmployee.setEmploymentStatus("FULL_TIME");

        testExpense = new Expense();
        testExpense.setId(UUID.randomUUID());
        testExpense.setEmployee(testEmployee);
        testExpense.setAmount(100.0);
        testExpense.setCategory("TRAVEL");
        testExpense.setDescription("Business trip");
        testExpense.setExpenseDate(LocalDateTime.now());
        testExpense.setStatus("PENDING");
        testExpense.setCreatedAt(LocalDateTime.now().minusDays(1));
        testExpense.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateExpense_Success() {
        // Arrange
        ExpenseCreateDTO dto = new ExpenseCreateDTO();
        dto.setEmployeeId(testEmployee.getId());
        dto.setAmount(150.0);
        dto.setCategory("FOOD");
        dto.setDescription("Lunch with client");
        dto.setExpenseDate(LocalDateTime.now());

        when(employeeRepository.findById(dto.getEmployeeId())).thenReturn(Optional.of(testEmployee));
        // Current user's employee record
        when(employeeRepository.findByUserId(currentUser.getId())).thenReturn(Optional.of(testEmployee));

        Expense expenseToSave = new Expense();
        expenseToSave.setId(UUID.randomUUID());
        expenseToSave.setEmployee(testEmployee);
        expenseToSave.setAmount(dto.getAmount());
        expenseToSave.setCategory(dto.getCategory());
        expenseToSave.setDescription(dto.getDescription());
        expenseToSave.setExpenseDate(dto.getExpenseDate());
        expenseToSave.setStatus("PENDING");

        when(expenseMapper.toEntity(dto)).thenReturn(expenseToSave);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Act
        ExpenseDTO result = expenseService.createExpense(dto, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getAmount(), result.getAmount());
        assertEquals(dto.getCategory(), result.getCategory());
        assertEquals(dto.getDescription(), result.getDescription());
        verify(auditLogService).log(eq(currentUser), eq("CREATE_EXPENSE"), eq("EXPENSE"), any(), anyString());
    }

    @Test
    void testCreateExpense_WrongUser_ThrowsException() {
        // Arrange
        ExpenseCreateDTO dto = new ExpenseCreateDTO();
        dto.setEmployeeId(testEmployee.getId());
        dto.setAmount(150.0);
        dto.setCategory("FOOD");
        dto.setDescription("Lunch with client");
        dto.setExpenseDate(LocalDateTime.now());

        // Current user is not the employee
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setName("Other User");
        otherUser.setEmail("other@example.com");
        otherUser.setRole(User.Role.EMPLOYEE);
        otherUser.setStatus(User.Status.ACTIVE);

        Employee otherEmployee = new Employee();
        otherEmployee.setId(UUID.randomUUID());
        otherEmployee.setEmployeeId("EMP002");
        otherEmployee.setUser(otherUser);
        // ... other fields

        when(employeeRepository.findById(dto.getEmployeeId())).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByUserId(otherUser.getId())).thenReturn(Optional.of(otherEmployee));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            expenseService.createExpense(dto, otherUser);
        });
        assertTrue(exception.getMessage().contains("Employees can only create expenses for themselves"));
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void testApproveOrRejectExpense_Approve_Success() {
        // Arrange
        UUID expenseId = testExpense.getId();
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));

        // Current user is a manager
        User managerUser = new User();
        managerUser.setId(UUID.randomUUID());
        managerUser.setName("Manager User");
        managerUser.setEmail("manager@example.com");
        managerUser.setRole(User.Role.MANAGER);
        managerUser.setStatus(User.Status.ACTIVE);

        Employee managerEmployee = new Employee();
        managerEmployee.setId(UUID.randomUUID());
        managerEmployee.setEmployeeId("MGR001");
        managerEmployee.setUser(managerUser);
        managerEmployee.setName("Manager Employee");
        managerEmployee.setEmail("manager@example.com");
        managerEmployee.setPhone("1111111111");
        managerEmployee.setDesignation("Manager");
        managerEmployee.setSalary(80000.0);
        managerEmployee.setDateOfJoining(LocalDateTime.now().minusYears(2));
        managerEmployee.setEmploymentStatus("FULL_TIME");

        when(employeeRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(managerEmployee));

        ExpenseApprovalDTO dto = new ExpenseApprovalDTO();
        dto.setStatus("APPROVED");
        dto.setComments("Approved");

        Expense updatedExpense = new Expense();
        updatedExpense.setId(testExpense.getId());
        updatedExpense.setEmployee(testExpense.getEmployee());
        updatedExpense.setAmount(testExpense.getAmount());
        updatedExpense.setCategory(testExpense.getCategory());
        updatedExpense.setDescription(testExpense.getDescription());
        updatedExpense.setExpenseDate(testExpense.getExpenseDate());
        updatedExpense.setStatus("APPROVED");
        updatedExpense.setApprovedBy(managerEmployee);
        updatedExpense.setApprovedAt(LocalDateTime.now());
        updatedExpense.setCreatedAt(testExpense.getCreatedAt());
        updatedExpense.setUpdatedAt(LocalDateTime.now());

        when(expenseRepository.save(any(Expense.class))).thenReturn(updatedExpense);

        // Act
        ExpenseDTO result = expenseService.approveOrRejectExpense(expenseId, dto, managerUser);

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        assertNotNull(result.getApprovedById());
        verify(auditLogService).log(eq(managerUser), eq("APPROVE_EXPENSE"), eq("EXPENSE"), eq(expenseId), anyString());
    }

    @Test
    void testApproveOrRejectExpense_NotPending_ThrowsException() {
        // Arrange
        testExpense.setStatus("APPROVED"); // already approved
        when(expenseRepository.findById(testExpense.getId())).thenReturn(Optional.of(testExpense));

        User managerUser = new User();
        managerUser.setId(UUID.randomUUID());
        managerUser.setName("Manager User");
        managerUser.setEmail("manager@example.com");
        managerUser.setRole(User.Role.MANAGER);
        managerUser.setStatus(User.Status.ACTIVE);

        Employee managerEmployee = new Employee();
        managerEmployee.setId(UUID.randomUUID());
        managerEmployee.setEmployeeId("MGR001");
        managerEmployee.setUser(managerUser);
        // ... other fields

        when(employeeRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(managerEmployee));

        ExpenseApprovalDTO dto = new ExpenseApprovalDTO();
        dto.setStatus("APPROVED");

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            expenseService.approveOrRejectExpense(testExpense.getId(), dto, managerUser);
        });
        assertTrue(exception.getMessage().contains("Expense is not in pending status"));
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }
}