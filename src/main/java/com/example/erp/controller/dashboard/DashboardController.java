package com.example.erp.controller.dashboard;

import com.example.erp.dto.dashboard.*;
import com.example.erp.entity.User;
import com.example.erp.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationCurrentUser;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dashboard & Analytics", description = "Dashboard and analytics APIs")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get overall summary metrics
     */
    @Operation(summary = "Get summary", description = "Get overall summary metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary metrics",
                    content = @Content(schema = @Schema(implementation = SummaryDTO.class)))
    })
    @GetMapping("/summary")
    @Cacheable(value = "dashboardSummary", key = "#root.method.name")
    public ResponseEntity<SummaryDTO> getSummary(@AuthenticationCurrentUser User currentUser) {
        log.info("Fetching dashboard summary by user: {}", currentUser.getEmail());
        SummaryDTO summary = dashboardService.getSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Get employee count per department
     */
    @Operation(summary = "Get employees per department", description = "Get employee count for each department")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employees per department",
                    content = @Content(schema = @Schema(implementation = EmployeesPerDepartmentDTO.class)))
    })
    @GetMapping("/employees-per-department")
    @Cacheable(value = "dashboardEmployeesPerDepartment", key = "#root.method.name")
    public ResponseEntity<java.util.List<EmployeesPerDepartmentDTO>> getEmployeesPerDepartment(@AuthenticationCurrentUser User currentUser) {
        log.info("Fetching employees per department by user: {}", currentUser.getEmail());
        java.util.List<EmployeesPerDepartmentDTO> list = dashboardService.getEmployeesPerDepartment();
        return ResponseEntity.ok(list);
    }

    /**
     * Get count of pending leave requests
     */
    @Operation(summary = "Get pending leaves count", description = "Get count of pending leave requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending leaves count",
                    content = @Content(schema = @Schema(implementation = PendingLeavesCountDTO.class)))
    })
    @GetMapping("/pending-leaves")
    @Cacheable(value = "dashboardPendingLeaves", key = "#root.method.name")
    public ResponseEntity<PendingLeavesCountDTO> getPendingLeavesCount(@AuthenticationCurrentUser User currentUser) {
        log.info("Fetching pending leaves count by user: {}", currentUser.getEmail());
        PendingLeavesCountDTO count = dashboardService.getPendingLeavesCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Get count of approved leave requests
     */
    @Operation(summary = "Get approved leaves count", description = "Get count of approved leave requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approved leaves count",
                    content = @Content(schema = @Schema(implementation = ApprovedLeavesCountDTO.class)))
    })
    @GetMapping("/approved-leaves")
    @Cacheable(value = "dashboardApprovedLeaves", key = "#root.method.name")
    public ResponseEntity<ApprovedLeavesCountDTO> getApprovedLeavesCount(@AuthenticationCurrentUser User currentUser) {
        log.info("Fetching approved leaves count by user: {}", currentUser.getEmail());
        ApprovedLeavesCountDTO count = dashboardService.getApprovedLeavesCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Get count of pending expense claims
     */
    @Operation(summary = "Get pending expenses count", description = "Get count of pending expense claims")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending expenses count",
                    content = @Content(schema = @Schema(implementation = PendingExpensesCountDTO.class)))
    })
    @GetMapping("/pending-expenses")
    @Cacheable(value = "dashboardPendingExpenses", key = "#root.method.name")
    public ResponseEntity<PendingExpensesCountDTO> getPendingExpensesCount(@AuthenticationCurrentUser User currentUser) {
        log.info("Fetching pending expenses count by user: {}", currentUser.getEmail());
        PendingExpensesCountDTO count = dashboardService.getPendingExpensesCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Get count of approved expense claims
     */
    @Operation(summary = "Get approved expenses count", description = "Get count of approved expense claims")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approved expenses count",
                    content = @Content(schema = @Schema(implementation = ApprovedExpensesCountDTO.class)))
    })
    @GetMapping("/approved-expenses")
    @Cacheable(value = "dashboardApprovedExpenses", key = "#root.method.name")
    public ResponseEntity<ApprovedExpensesCountDTO> getApprovedExpensesCount(@AuthenticationCurrentUser User currentUser) {
        log.info("Fetching approved expenses count by user: {}", currentUser.getEmail());
        ApprovedExpensesCountDTO count = dashboardService.getApprovedExpensesCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Get monthly expense trends for the last 6 months
     */
    @Operation(summary = "Get monthly expense trends", description = "Get monthly expense trends for the last 6 months")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly expense trends",
                    content = @Content(schema = @Schema(implementation = MonthlyExpenseTrendsDTO.class)))
    })
    @GetMapping("/monthly-expense-trends")
    @Cacheable(value = "dashboardMonthlyExpenseTrends", key = "#root.method.name")
    public ResponseEntity<java.util.List<MonthlyExpenseTrendsDTO>> getMonthlyExpenseTrends(@AuthenticationCurrentUser User currentUser) {
        log.info("Fetching monthly expense trends by user: {}", currentUser.getEmail());
        java.util.List<MonthlyExpenseTrendsDTO> list = dashboardService.getMonthlyExpenseTrends();
        return ResponseEntity.ok(list);
    }

    /**
     * Get department expense breakdown for the current month
     */
    @Operation(summary = "Get department expense breakdown", description = "Get department expense breakdown for the current month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Department expense breakdown",
                    content = @Content(schema = @Schema(implementation = DepartmentExpenseBreakdownDTO.class)))
    })
    @GetMapping("/department-expense-breakdown")
    @Cacheable(value = "dashboardDepartmentExpenseBreakdown", key = "#root.method.name")
    public ResponseEntity<java.util.List<DepartmentExpenseBreakdownDTO>> getDepartmentExpenseBreakdown(@AuthenticationCurrentUser User currentUser) {
        log.info("Fetching department expense breakdown by user: {}", currentUser.getEmail());
        java.util.List<DepartmentExpenseBreakdownDTO> list = dashboardService.getDepartmentExpenseBreakdown();
        return ResponseEntity.ok(list);
    }

    /**
     * Get count of new employees this month
     */
    @Operation(summary = "Get new employees this month", description = "Get count of new employees this month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New employees this month",
                    content = @Content(schema = @Schema(implementation = NewEmployeesThisMonthDTO.class)))
    })
    @GetMapping("/new-employees-month")
    @Cacheable(value = "dashboardNewEmployeesThisMonth", key = "#root.method.name")
    public ResponseEntity<NewEmployeesThisMonthDTO> getNewEmployeesThisMonth(@AuthenticationCurrentUser User currentUser) {
        log.info("Fetching new employees this month by user: {}", currentUser.getEmail());
        NewEmployeesThisMonthDTO count = dashboardService.getNewEmployeesThisMonth();
        return ResponseEntity.ok(count);
    }
}