package com.example.erp.service;

import com.example.erp.dto.dashboard.*;
import com.example.erp.entity.*;
import com.example.erp.repository.*;
import org.junit.jupiter.BeforeEach;
import org.junit.jupiter.Test;
import org.junit.jupiter.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardService
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private List<Employee> testEmployees;
    private List<Department> testDepartments;
    private List<LeaveRequest> testLeaveRequests;
    private List<Expense> testExpenses;

    @BeforeEach
    void setUp() {
        // Setup test data
        UUID emp1Id = UUID.randomUUID();
        UUID emp2Id = UUID.randomUUID();
        UUID dept1Id = UUID.randomUUID();
        UUID dept2Id = UUID.randomUUID();

        Employee emp1 = new Employee();
        emp1.setId(emp1Id);
        emp1.setEmployeeId("EMP001");
        emp1.setName("Employee One");
        emp1.setEmail("emp1@example.com");
        emp1.setSalary(50000.0);
        emp1.setDateOfJoining(LocalDateTime.now().minusYears(2));
        emp1.setEmploymentStatus("FULL_TIME");

        Employee emp2 = new Employee();
        emp2.setId(emp2Id);
        emp2.setEmployeeId("EMP002");
        emp2.setName("Employee Two");
        emp2.setEmail("emp2@example.com");
        emp2.setSalary(60000.0);
        emp2.setDateOfJoining(LocalDateTime.now().minusYears(1));
        emp2.setEmploymentStatus("FULL_TIME");

        testEmployees = List.of(emp1, emp2);

        Department dept1 = new Department();
        dept1.setId(dept1Id);
        dept1.setName("Department One");
        dept1.setBudget(100000.0);
        dept1.setCreatedAt(LocalDateTime.now().minusDays(1));
        dept1.setUpdatedAt(LocalDateTime.now());

        Department dept2 = new Department();
        dept2.setId(dept2Id);
        dept2.setName("Department Two");
        dept2.setBudget(150000.0);
        dept2.setCreatedAt(LocalDateTime.now().minusDays(1));
        dept2.setUpdatedAt(LocalDateTime.now());

        testDepartments = List.of(dept1, dept2);

        // Assign employees to departments
        emp1.setDepartment(dept1);
        emp2.setDepartment(dept2);

        // Leave requests
        LeaveRequest lr1 = new LeaveRequest();
        lr1.setId(UUID.randomUUID());
        lr1.setEmployee(emp1);
        lr1.setLeaveType("VACATION");
        lr1.setStartDate(LocalDateTime.now().plusDays(5));
        lr1.setEndDate(LocalDateTime.now().plusDays(10));
        lr1.setReason("Vacation");
        lr1.setStatus("PENDING");
        lr1.setCreatedAt(LocalDateTime.now());
        lr1.setUpdatedAt(LocalDateTime.now());

        LeaveRequest lr2 = new LeaveRequest();
        lr2.setId(UUID.randomUUID());
        lr2.setEmployee(emp2);
        lr2.setLeaveType("SICK");
        lr2.setStartDate(LocalDateTime.now().plusDays(2));
        lr2.setEndDate(LocalDateTime.now().plusDays(3));
        lr2.setReason("Sick leave");
        lr2.setStatus("APPROVED");
        lr2.setCreatedAt(LocalDateTime.now());
        lr2.setUpdatedAt(LocalDateTime.now());

        testLeaveRequests = List.of(lr1, lr2);

        // Expenses
        Expense exp1 = new Expense();
        exp1.setId(UUID.randomUUID());
        exp1.setEmployee(emp1);
        exp1.setAmount(100.0);
        exp1.setCategory("TRAVEL");
        exp1.setDescription("Trip");
        exp1.setExpenseDate(LocalDateTime.now().minusDays(5));
        exp1.setStatus("PENDING");
        exp1.setCreatedAt(LocalDateTime.now());
        exp1.setUpdatedAt(LocalDateTime.now());

        Expense exp2 = new Expense();
        exp2.setId(UUID.randomUUID());
        exp2.setEmployee(emp2);
        exp2.setAmount(200.0);
        exp2.setCategory("FOOD");
        exp2.setDescription("Lunch");
        exp2.setExpenseDate(LocalDateTime.now().minusDays(3));
        exp2.setStatus("APPROVED");
        exp2.setCreatedAt(LocalDateTime.now());
        exp2.setUpdatedAt(LocalDateTime.now());

        testExpenses = List.of(exp1, exp2);
    }

    @Test
    void testGetSummary() {
        // Arrange
        when(employeeRepository.count()).thenReturn(2L);
        when(departmentRepository.count()).thenReturn(2L);
        when(leaveRequestRepository.countByStatus("PENDING")).thenReturn(1L);
        when(leaveRequestRepository.countByStatus("APPROVED")).thenReturn(1L);
        when(expenseRepository.countByStatus("PENDING")).thenReturn(1L);
        when(expenseRepository.countByStatus("APPROVED")).thenReturn(1L);

        // Act
        SummaryDTO result = dashboardService.getSummary();

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getTotalEmployees());
        assertEquals(2L, result.getTotalDepartments());
        assertEquals(1L, result.getTotalPendingLeaves());
        assertEquals(1L, result.getTotalApprovedLeaves());
        assertEquals(1L, result.getTotalPendingExpenses());
        assertEquals(1L, result.getTotalApprovedExpenses());
    }

    @Test
    void testGetEmployeesPerDepartment() {
        // Arrange
        when(employeeRepository.countEmployeesPerDepartment()).thenReturn(List.of(
                new Object[]{testDepartments.get(0).getId(), testDepartments.get(0).getName(), 1L},
                new Object[]{testDepartments.get(1).getId(), testDepartments.get(1).getName(), 1L}
        ));

        // Act
        List<EmployeesPerDepartmentDTO> result = dashboardService.getEmployeesPerDepartment();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testDepartments.get(0).getId(), result.get(0).getDepartmentId());
        assertEquals(testDepartments.get(0).getName(), result.get(0).getDepartmentName());
        assertEquals(1L, result.get(0).getEmployeeCount());
        assertEquals(testDepartments.get(1).getId(), result.get(1).getDepartmentId());
        assertEquals(testDepartments.get(1).getName(), result.get(1).getDepartmentName());
        assertEquals(1L, result.get(1).getEmployeeCount());
    }

    @Test
    void testGetPendingLeavesCount() {
        // Arrange
        when(leaveRequestRepository.countByStatus("PENDING")).thenReturn(1L);

        // Act
        PendingLeavesCountDTO result = dashboardService.getPendingLeavesCount();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getCount());
    }

    @Test
    void testGetApprovedLeavesCount() {
        // Arrange
        when(leaveRequestRepository.countByStatus("APPROVED")).thenReturn(1L);

        // Act
        ApprovedLeavesCountDTO result = dashboardService.getApprovedLeavesCount();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getCount());
    }

    @Test
    void testGetPendingExpensesCount() {
        // Arrange
        when(expenseRepository.countByStatus("PENDING")).thenReturn(1L);

        // Act
        PendingExpensesCountDTO result = dashboardService.getPendingExpensesCount();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getCount());
    }

    @Test
    void testGetApprovedExpensesCount() {
        // Arrange
        when(expenseRepository.countByStatus("APPROVED")).thenReturn(1L);

        // Act
        ApprovedExpensesCountDTO result = dashboardService.getApprovedExpensesCount();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getCount());
    }

    @Test
    void testGetMonthlyExpenseTrends() {
        // Arrange
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        when(expenseRepository.getMonthlyExpenseTrends(sixMonthsAgo)).thenReturn(List.of(
                new Object[]{LocalDateTime.now().minusMonths(5).withDayOfMonth(1), 150.0},
                new Object[]{LocalDateTime.now().minusMonths(4).withDayOfMonth(1), 200.0}
        ));

        // Act
        List<MonthlyExpenseTrendsDTO> result = dashboardService.getMonthlyExpenseTrends();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(150.0, result.get(0).getTotalExpense(), 0.01);
        assertEquals(200.0, result.get(1).getTotalExpense(), 0.01);
    }

    @Test
    void testGetDepartmentExpenseBreakdown() {
        // Arrange
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        when(expenseRepository.getDepartmentExpenseBreakdown(startOfMonth, endOfMonth)).thenReturn(List.of(
                new Object[]{testDepartments.get(0).getId(), testDepartments.get(0).getName(), 100.0},
                new Object[]{testDepartments.get(1).getId(), testDepartments.get(1).getName(), 200.0}
        ));

        // Act
        List<DepartmentExpenseBreakdownDTO> result = dashboardService.getDepartmentExpenseBreakdown();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testDepartments.get(0).getId(), result.get(0).getDepartmentId());
        assertEquals(testDepartments.get(0).getName(), result.get(0).getDepartmentName());
        assertEquals(100.0, result.get(0).getTotalExpense(), 0.01);
        assertEquals(testDepartments.get(1).getId(), result.get(1).getDepartmentId());
        assertEquals(testDepartments.get(1).getName(), result.get(1).getDepartmentName());
        assertEquals(200.0, result.get(1).getTotalExpense(), 0.01);
    }

    @Test
    void testGetNewEmployeesThisMonth() {
        // Arrange
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        when(employeeRepository.countNewEmployeesSince(startOfMonth)).thenReturn(1L);

        // Act
        NewEmployeesThisMonthDTO result = dashboardService.getNewEmployeesThisMonth();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getCount());
    }
}