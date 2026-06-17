package com.example.erp.service;

import com.example.erp.dto.aicopilot.AiCopilotRequestDTO;
import com.example.erp.dto.aicopilot.AiCopilotResponseDTO;
import com.example.erp.entity.*;
import com.example.erp.repository.*;
import org.junit.jupiter.BeforeEach;
import org.junit.jupiter.Test;
import org.junit.jupiter.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AiCopilotService
 */
@ExtendWith(MockitoExtension.class)
class AiCopilotServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private org.springframework.web.reactive.function.client.WebClient webClient;

    @InjectMocks
    private AiCopilotService aiCopilotService;

    private User currentUser;
    private Employee testEmployee;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        // Set the Gemini API key via reflection
        ReflectionTestUtils.setField(aiCopilotService, "geminiApiKey", "test-api-key");

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

        testDepartment = new Department();
        testDepartment.setId(UUID.randomUUID());
        testDepartment.setName("Engineering");
        testDepartment.setBudget(500000.0);
        testDepartment.setDescription("Engineering department");
    }

    @Test
    void testProcessQuestion_DepartmentMostExpense() {
        // Arrange
        AiCopilotRequestDTO request = new AiCopilotRequestDTO();
        request.setQuestion("Which department spent the most this month?");

        // Mock the expense repository to return a department with highest expense
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        when(expenseRepository.getDepartmentExpenseBreakdown(startOfMonth, endOfMonth)).thenReturn(List.of(
                new Object[]{testDepartment.getId(), testDepartment.getName(), 10000.0},
                new Object[]{UUID.randomUUID(), "Sales", 5000.0}
        ));

        // Act
        AiCopilotResponseDTO result = aiCopilotService.processQuestion(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAnswer().contains("Engineering"));
        assertTrue(result.getAnswer().contains("spent the most"));
        assertTrue(result.getAnswer().contains("10000.0"));
        assertTrue(result.getSuggestions().length > 0);
    }

    @Test
    void testProcessQuestion_TotalExpenseThisMonth() {
        // Arrange
        AiCopilotRequestDTO request = new AiCopilotRequestDTO();
        request.setQuestion("What was the total expense this month?");

        // Mock the expense repository to return total expense
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        when(expenseRepository.getTotalExpenseBetween(startOfMonth, endOfMonth)).thenReturn(15000.0);

        // Act
        AiCopilotResponseDTO result = aiCopilotService.processQuestion(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAnswer().contains("total expense this month"));
        assertTrue(result.getAnswer().contains("15000.0"));
        assertTrue(result.getSuggestions().length > 0);
    }

    @Test
    void testProcessQuestion_PendingLeaves() {
        // Arrange
        AiCopilotRequestDTO request = new AiCopilotRequestDTO();
        request.setQuestion("How many pending leave requests are there?");

        // Mock the leave request repository
        when(leaveRequestRepository.countByStatus("PENDING")).thenReturn(5L);

        // Act
        AiCopilotResponseDTO result = aiCopilotService.processQuestion(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAnswer().contains("5"));
        assertTrue(result.getAnswer().contains("pending leave requests"));
        assertTrue(result.getSuggestions().length > 0);
    }

    @Test
    void testProcessQuestion_NewEmployeesThisMonth() {
        // Arrange
        AiCopilotRequestDTO request = new AiCopilotRequestDTO();
        request.setQuestion("How many new employees this month?");

        // Mock the employee repository
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        when(employeeRepository.countNewEmployeesSince(startOfMonth)).thenReturn(3L);

        // Act
        AiCopilotResponseDTO result = aiCopilotService.processQuestion(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAnswer().contains("3"));
        assertTrue(result.getAnswer().contains("new employees this month"));
        assertTrue(result.getSuggestions().length > 0);
    }

    @Test
    void testProcessQuestion_UnknownQuestion() {
        // Arrange
        AiCopilotRequestDTO request = new AiCopilotRequestDTO();
        request.setQuestion("What is the meaning of life?");

        // Act
        AiCopilotResponseDTO result = aiCopilotService.processQuestion(request);

        // Assert
        assertNotNull(result);
        // The service should return a default response for unknown questions
        assertTrue(result.getAnswer().contains("I'm sorry, I don't have enough information"));
        assertTrue(result.getSuggestions().length > 0);
    }

    @Test
    void testProcessQuestion_Greeting() {
        // Arrange
        AiCopilotRequestDTO request = new AiCopilotRequestDTO();
        request.setQuestion("Hello");

        // Act
        AiCopilotResponseDTO result = aiCopilotService.processQuestion(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getAnswer().contains("Hello! I'm your AI Copilot"));
        assertTrue(result.getSuggestions().length > 0);
    }
}