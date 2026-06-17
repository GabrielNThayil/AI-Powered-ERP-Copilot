package com.example.erp.service;

import com.example.erp.dto.leave.LeaveRequestApprovalDTO;
import com.example.erp.dto.leave.LeaveRequestCreateDTO;
import com.example.erp.dto.leave.LeaveRequestDTO;
import com.example.erp.entity.LeaveRequest;
import com.example.erp.entity.Employee;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.LeaveRequestRepository;
import com.example.erp.repository.EmployeeRepository;
import com.example.erp.mapper.LeaveRequestMapper;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LeaveRequestService
 */
@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRequestMapper leaveRequestMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    private User currentUser;
    private Employee testEmployee;
    private LeaveRequest testLeaveRequest;

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
        testEmployee.setUser(currentUser); // The user is the employee
        testEmployee.setName("Test Employee");
        testEmployee.setEmail("employee@example.com");
        testEmployee.setPhone("1234567890");
        testEmployee.setDesignation("Test Engineer");
        testEmployee.setSalary(50000.0);
        testEmployee.setDateOfJoining(LocalDateTime.now().minusYears(1));
        testEmployee.setEmploymentStatus("FULL_TIME");

        testLeaveRequest = new LeaveRequest();
        testLeaveRequest.setId(UUID.randomUUID());
        testLeaveRequest.setEmployee(testEmployee);
        testLeaveRequest.setLeaveType("VACATION");
        testLeaveRequest.setStartDate(LocalDateTime.now().plusDays(10));
        testLeaveRequest.setEndDate(LocalDateTime.now().plusDays(15));
        testLeaveRequest.setReason("Vacation request");
        testLeaveRequest.setStatus("PENDING");
        testLeaveRequest.setCreatedAt(LocalDateTime.now().minusDays(1));
        testLeaveRequest.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateLeaveRequest_Success() {
        // Arrange
        LeaveRequestCreateDTO dto = new LeaveRequestCreateDTO();
        dto.setEmployeeId(testEmployee.getId());
        dto.setLeaveType("VACATION");
        dto.setStartDate(LocalDateTime.now().plusDays(10));
        dto.setEndDate(LocalDateTime.now().plusDays(15));
        dto.setReason("Vacation request");

        when(employeeRepository.findById(dto.getEmployeeId())).thenReturn(Optional.of(testEmployee));
        // Current user's employee record
        when(employeeRepository.findByUserId(currentUser.getId())).thenReturn(Optional.of(testEmployee));
        // Validate dates
        // dto.getEndDate().isAfter(dto.getStartDate()) -> true

        LeaveRequest leaveRequestToSave = new LeaveRequest();
        leaveRequestToSave.setId(UUID.randomUUID());
        leaveRequestToSave.setEmployee(testEmployee);
        leaveRequestToSave.setLeaveType(dto.getLeaveType());
        leaveRequestToSave.setStartDate(dto.getStartDate());
        leaveRequestToSave.setEndDate(dto.getEndDate());
        dto.setReason(dto.getReason());
        leaveRequestToSave.setStatus("PENDING");

        when(leaveRequestMapper.toEntity(dto)).thenReturn(leaveRequestToSave);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Act
        LeaveRequestDTO result = leaveRequestService.createLeaveRequest(dto, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getLeaveType(), result.getLeaveType());
        assertEquals(dto.getReason(), result.getReason());
        verify(auditLogService).log(eq(currentUser), eq("CREATE_LEAVE_REQUEST"), eq("LEAVE_REQUEST"), any(), anyString());
    }

    @Test
    void testCreateLeaveRequest_WrongUser_ThrowsException() {
        // Arrange
        LeaveRequestCreateDTO dto = new LeaveRequestCreateDTO();
        dto.setEmployeeId(testEmployee.getId());
        dto.setLeaveType("VACATION");
        dto.setStartDate(LocalDateTime.now().plusDays(10));
        dto.setEndDate(LocalDateTime.now().plusDays(15));
        dto.setReason("Vacation request");

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
            leaveRequestService.createLeaveRequest(dto, otherUser);
        });
        assertTrue(exception.getMessage().contains("Employees can only create leave requests for themselves"));
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void testApproveOrRejectLeaveRequest_Approve_Success() {
        // Arrange
        UUID leaveRequestId = testLeaveRequest.getId();
        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(testLeaveRequest));

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

        LeaveRequestApprovalDTO dto = new LeaveRequestApprovalDTO();
        dto.setStatus("APPROVED");
        dto.setComments("Looks good");
        // approvedAt will be set to now in service

        LeaveRequest updatedLeaveRequest = new LeaveRequest();
        updatedLeaveRequest.setId(testLeaveRequest.getId());
        updatedLeaveRequest.setEmployee(testLeaveRequest.getEmployee());
        updatedLeaveRequest.setLeaveType(testLeaveRequest.getLeaveType());
        updatedLeaveRequest.setStartDate(testLeaveRequest.getStartDate());
        updatedLeaveRequest.setEndDate(testLeaveRequest.getEndDate());
        updatedLeaveRequest.setReason(testLeaveRequest.getReason());
        updatedLeaveRequest.setStatus("APPROVED");
        updatedLeaveRequest.setApprovedBy(managerEmployee);
        updatedLeaveRequest.setApprovedAt(LocalDateTime.now());
        updatedLeaveRequest.setCreatedAt(testLeaveRequest.getCreatedAt());
        updatedLeaveRequest.setUpdatedAt(LocalDateTime.now());

        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(updatedLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveRequestService.approveOrRejectLeaveRequest(leaveRequestId, dto, managerUser);

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        assertNotNull(result.getApprovedById());
        verify(auditLogService).log(eq(managerUser), eq("APPROVE_LEAVE_REQUEST"), eq("LEAVE_REQUEST"), eq(leaveRequestId), anyString());
    }

    @Test
    void testApproveOrRejectLeaveRequest_NotPending_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus("APPROVED"); // already approved
        when(leaveRequestRepository.findById(testLeaveRequest.getId())).thenReturn(Optional.of(testLeaveRequest));

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

        LeaveRequestApprovalDTO dto = new LeaveRequestApprovalDTO();
        dto.setStatus("APPROVED");

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            leaveRequestService.approveOrRejectLeaveRequest(testLeaveRequest.getId(), dto, managerUser);
        });
        assertTrue(exception.getMessage().contains("Leave request is not in pending status"));
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void testCancelLeaveRequest_Success() {
        // Arrange
        when(leaveRequestRepository.findById(testLeaveRequest.getId())).thenReturn(Optional.of(testLeaveRequest));

        // Current user is the employee
        when(employeeRepository.findByUserId(currentUser.getId())).thenReturn(Optional.of(testEmployee));

        LeaveRequest updatedLeaveRequest = new LeaveRequest();
        updatedLeaveRequest.setId(testLeaveRequest.getId());
        updatedLeaveRequest.setEmployee(testLeaveRequest.getEmployee());
        updatedLeaveRequest.setLeaveType(testLeaveRequest.getLeaveType());
        updatedLeaveRequest.setStartDate(testLeaveRequest.getStartDate());
        updatedLeaveRequest.setEndDate(testLeaveRequest.getEndDate());
        updatedLeaveRequest.setReason(testLeaveRequest.getReason());
        updatedLeaveRequest.setStatus("CANCELLED"); // We set to CANCELLED in service
        updatedLeaveRequest.setApprovedBy(null);
        updatedLeaveRequest.setApprovedAt(null);
        updatedLeaveRequest.setCreatedAt(testLeaveRequest.getCreatedAt());
        updatedLeaveRequest.setUpdatedAt(LocalDateTime.now());

        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(updatedLeaveRequest);

        // Act
        LeaveRequestDTO result = leaveRequestService.cancelLeaveRequest(testLeaveRequest.getId(), currentUser);

        // Assert
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        verify(auditLogService).log(eq(currentUser), eq("CANCEL_LEAVE_REQUEST"), eq("LEAVE_REQUEST"), eq(testLeaveRequest.getId()), anyString());
    }

    @Test
    void testCancelLeaveRequest_NotPending_ThrowsException() {
        // Arrange
        testLeaveRequest.setStatus("APPROVED"); // not pending
        when(leaveRequestRepository.findById(testLeaveRequest.getId())).thenReturn(Optional.of(testLeaveRequest));

        // Current user is the employee
        when(employeeRepository.findByUserId(currentUser.getId())).thenReturn(Optional.of(testEmployee));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            leaveRequestService.cancelLeaveRequest(testLeaveRequest.getId(), currentUser);
        });
        assertTrue(exception.getMessage().contains("Only pending leave requests can be cancelled"));
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }
}