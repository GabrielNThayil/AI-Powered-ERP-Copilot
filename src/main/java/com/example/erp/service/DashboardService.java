package com.example.erp.service;

import com.example.erp.dto.dashboard.*;
import com.example.erp.entity.*;
import com.example.erp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for dashboard analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ExpenseRepository expenseRepository;

    /**
     * Get overall summary metrics
     */
    public SummaryDTO getSummary() {
        Long totalEmployees = employeeRepository.count();
        Long totalDepartments = departmentRepository.count();
        Long totalPendingLeaves = leaveRequestRepository.countByStatus("PENDING");
        Long totalApprovedLeaves = leaveRequestRepository.countByStatus("APPROVED");
        Long totalPendingExpenses = expenseRepository.countByStatus("PENDING");
        Long totalApprovedExpenses = expenseRepository.countByStatus("APPROVED");

        return SummaryDTO.builder()
                .totalEmployees(totalEmployees)
                .totalDepartments(totalDepartments)
                .totalPendingLeaves(totalPendingLeaves)
                .totalApprovedLeaves(totalApprovedLeaves)
                .totalPendingExpenses(totalPendingExpenses)
                .totalApprovedExpenses(totalApprovedExpenses)
                .build();
    }

    /**
     * Get employee count per department
     */
    public java.util.List<EmployeesPerDepartmentDTO> getEmployeesPerDepartment() {
        java.util.List<Object[]> results = employeeRepository.countEmployeesPerDepartment();
        java.util.List<EmployeesPerDepartmentDTO> dtos = new ArrayList<>();
        for (Object[] row : results) {
            UUID departmentId = (UUID) row[0];
            String departmentName = (String) row[1];
            Long employeeCount = ((Number) row[2]).longValue();
            dtos.add(EmployeesPerDepartmentDTO.builder()
                    .departmentId(departmentId)
                    .departmentName(departmentName)
                    .employeeCount(employeeCount)
                    .build());
        }
        return dtos;
    }

    /**
     * Get count of pending leave requests
     */
    public PendingLeavesCountDTO getPendingLeavesCount() {
        Long count = leaveRequestRepository.countByStatus("PENDING");
        return PendingLeavesCountDTO.builder()
                .count(count)
                .build();
    }

    /**
     * Get count of approved leave requests (optional date filtering)
     * For simplicity, we'll return the count for all time.
     * In a real app, we might filter by current year or as per parameters.
     */
    public ApprovedLeavesCountDTO getApprovedLeavesCount() {
        Long count = leaveRequestRepository.countByStatus("APPROVED");
        return ApprovedLeavesCountDTO.builder()
                .count(count)
                .build();
    }

    /**
     * Get count of pending expense claims
     */
    public PendingExpensesCountDTO getPendingExpensesCount() {
        Long count = expenseRepository.countByStatus("PENDING");
        return PendingExpensesCountDTO.builder()
                .count(count)
                .build();
    }

    /**
     * Get count of approved expense claims (optional date filtering)
     */
    public ApprovedExpensesCountDTO getApprovedExpensesCount() {
        Long count = expenseRepository.countByStatus("APPROVED");
        return ApprovedExpensesCountDTO.builder()
                .count(count)
                .build();
    }

    /**
     * Get monthly expense trends for the last 6 months
     */
    public java.util.List<MonthlyExpenseTrendsDTO> getMonthlyExpenseTrends() {
        // We'll get expenses from the last 6 months
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        java.util.List<Object[]> results = expenseRepository.getMonthlyExpenseTrends(sixMonthsAgo);
        java.util.List<MonthlyExpenseTrendsDTO> dtos = new ArrayList<>();
        for (Object[] row : results) {
            LocalDateTime month = (LocalDateTime) row[0];
            Double totalExpense = ((Number) row[1]).doubleValue();
            dtos.add(MonthlyExpenseTrendsDTO.builder()
                    .month(month)
                    .totalExpense(totalExpense)
                    .build());
        }
        return dtos;
    }

    /**
     * Get department expense breakdown for a given period (e.g., current month)
     * For simplicity, we'll compute for the current month.
     */
    public java.util.List<DepartmentExpenseBreakdownDTO> getDepartmentExpenseBreakdown() {
        // We'll compute for the current month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        java.util.List<Object[]> results = expenseRepository.getDepartmentExpenseBreakdown(startOfMonth, endOfMonth);
        java.util.List<DepartmentExpenseBreakdownDTO> dtos = new ArrayList<>();
        for (Object[] row : results) {
            UUID departmentId = (UUID) row[0];
            String departmentName = (String) row[1];
            Double totalExpense = ((Number) row[2]).doubleValue();
            dtos.add(DepartmentExpenseBreakdownDTO.builder()
                    .departmentId(departmentId)
                    .departmentName(departmentName)
                    .totalExpense(totalExpense)
                    .build());
        }
        return dtos;
    }

    /**
     * Get count of new employees this month
     */
    public NewEmployeesThisMonthDTO getNewEmployeesThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Long count = employeeRepository.countNewEmployeesSince(startOfMonth);
        return NewEmployeesThisMonthDTO.builder()
                .count(count)
                .build();
    }
}