package com.example.erp.controller.leave;

import com.example.erp.dto.leave.LeaveRequestApprovalDTO;
import com.example.erp.dto.leave.LeaveRequestCreateDTO;
import com.example.erp.dto.leave.LeaveRequestDTO;
import com.example.erp.dto.employee.EmployeeDTO;
import com.example.erp.entity.User;
import com.example.erp.service.LeaveRequestService;
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

@Tag(name = "Leave Management", description = "Leave management APIs")
@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * Create a new leave request
     * Employees can create leave requests for themselves
     */
    @Operation(summary = "Create a new leave request", description = "Create a new leave request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Leave request created",
                    content = @Content(schema = @Schema(implementation = LeaveRequestDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    public ResponseEntity<LeaveRequestDTO> createLeaveRequest(@Valid @RequestBody LeaveRequestCreateDTO dto,
                                                              @AuthenticationCurrentUser User currentUser) {
        log.info("Creating leave request for employee ID: {} by user: {}", dto.getEmployeeId(), currentUser.getEmail());
        LeaveRequestDTO createdLeaveRequest = leaveRequestService.createLeaveRequest(dto, currentUser);
        return ResponseEntity.status(201).body(createdLeaveRequest);
    }

    /**
     * Get leave request by ID
     */
    @Operation(summary = "Get leave request by ID", description = "Get leave request details by internal ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leave request found",
                    content = @Content(schema = @Schema(implementation = LeaveRequestDTO.class))),
            @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestDTO> getLeaveRequestById(@PathVariable UUID id,
                                                               @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching leave request with ID: {} by user: {}", id, currentUser.getEmail());
        LeaveRequestDTO leaveRequest = leaveRequestService.getLeaveRequestById(id);
        return ResponseEntity.ok(leaveRequest);
    }

    /**
     * Get leave requests by employee ID
     * Employees can see their own leave requests, managers can see their department's, admins can see all.
     */
    @Operation(summary = "Get leave requests by employee ID", description = "Get leave requests for a specific employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of leave requests"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<java.util.List<LeaveRequestDTO>> getLeaveRequestsByEmployeeId(@PathVariable UUID employeeId,
                                                                                        @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching leave requests for employee ID: {} by user: {}", employeeId, currentUser.getEmail());
        java.util.List<LeaveRequestDTO> leaveRequests = leaveRequestService.getLeaveRequestsByEmployeeId(employeeId);
        return ResponseEntity.ok(leaveRequests);
    }

    /**
     * Get leave requests by status
     */
    @Operation(summary = "Get leave requests by status", description = "Get leave requests by their status (PENDING, APPROVED, REJECTED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of leave requests"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<java.util.List<LeaveRequestDTO>> getLeaveRequestsByStatus(@PathVariable String status,
                                                                                    @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching leave requests with status: {} by user: {}", status, currentUser.getEmail());
        java.util.List<LeaveRequestDTO> leaveRequests = leaveRequestService.getLeaveRequestsByStatus(status);
        return ResponseEntity.ok(leaveRequests);
    }

    /**
     * Get leave requests by date range
     */
    @Operation(summary = "Get leave requests by date range", description = "Get leave requests within a date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of leave requests"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @GetMapping("/date-range")
    public ResponseEntity<java.util.List<LeaveRequestDTO>> getLeaveRequestsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching leave requests from {} to {} by user: {}", startDate, endDate, currentUser.getEmail());
        java.util.List<LeaveRequestDTO> leaveRequests = leaveRequestService.getLeaveRequestsByDateRange(startDate, endDate);
        return ResponseEntity.ok(leaveRequests);
    }

    /**
     * Get all leave requests with pagination, sorting, and filtering
     */
    @Operation(summary = "Get all leave requests", description = "Get all leave requests with pagination, sorting, and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of leave requests"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping
    public ResponseEntity<Page<LeaveRequestDTO>> getAllLeaveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching leave requests with filters by user: {}", currentUser.getEmail());
        Page<LeaveRequestDTO> leaveRequests = leaveRequestService.getAllLeaveRequests(page, size, sortBy, direction,
                employeeId, status, startDate, endDate, currentUser);
        return ResponseEntity.ok(leaveRequests);
    }

    /**
     * Approve a leave request
     * Only managers and admins can approve leave requests
     */
    @Operation(summary = "Approve a leave request", description = "Approve a pending leave request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leave request approved",
                    content = @Content(schema = @Schema(implementation = LeaveRequestDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Leave request not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestDTO> approveLeaveRequest(@PathVariable UUID id,
                                                               @Valid @RequestBody LeaveRequestApprovalDTO dto,
                                                               @AuthenticationCurrentUser User currentUser) {
        log.info("Approving leave request with ID: {} by user: {}", id, currentUser.getEmail());
        // Set status to APPROVED
        dto.setStatus("APPROVED");
        LeaveRequestDTO approvedLeaveRequest = leaveRequestService.approveOrRejectLeaveRequest(id, dto, currentUser);
        return ResponseEntity.ok(approvedLeaveRequest);
    }

    /**
     * Reject a leave request
     * Only managers and admins can reject leave requests
     */
    @Operation(summary = "Reject a leave request", description = "Reject a pending leave request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leave request rejected",
                    content = @Content(schema = @Schema(implementation = LeaveRequestDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Leave request not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestDTO> rejectLeaveRequest(@PathVariable UUID id,
                                                              @Valid @RequestBody LeaveRequestApprovalDTO dto,
                                                              @AuthenticationCurrentUser User currentUser) {
        log.info("Rejecting leave request with ID: {} by user: {}", id, currentUser.getEmail());
        // Set status to REJECTED
        dto.setStatus("REJECTED");
        LeaveRequestDTO rejectedLeaveRequest = leaveRequestService.approveOrRejectLeaveRequest(id, dto, currentUser);
        return ResponseEntity.ok(rejectedLeaveRequest);
    }

    /**
     * Cancel a leave request
     * Only the employee who created the request can cancel it (if pending)
     */
    @Operation(summary = "Cancel a leave request", description = "Cancel a pending leave request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leave request cancelled",
                    content = @Content(schema = @Schema(implementation = LeaveRequestDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Leave request not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestDTO> cancelLeaveRequest(@PathVariable UUID id,
                                                              @AuthenticationCurrentUser User currentUser) {
        log.info("Cancelling leave request with ID: {} by user: {}", id, currentUser.getEmail());
        LeaveRequestDTO cancelledLeaveRequest = leaveRequestService.cancelLeaveRequest(id, currentUser);
        return ResponseEntity.ok(cancelledLeaveRequest);
    }
}