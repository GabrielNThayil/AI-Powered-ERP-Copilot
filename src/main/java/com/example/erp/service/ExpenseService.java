package com.example.erp.service;

import com.example.erp.dto.expense.ExpenseApprovalDTO;
import com.example.erp.dto.expense.ExpenseCreateDTO;
import com.example.erp.dto.expense.ExpenseDTO;
import com.example.erp.dto.employee.EmployeeDTO;
import com.example.erp.entity.Expense;
import com.example.erp.entity.Employee;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.ExpenseRepository;
import com.example.erp.repository.EmployeeRepository;
import com.example.erp.mapper.ExpenseMapper;
import com.example.erp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Expense management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;
    private final ExpenseMapper expenseMapper;
    private final AuditLogService auditLogService;

    /**
     * Create a new expense
     */
    public ExpenseDTO createExpense(ExpenseCreateDTO dto, User currentUser) {
        // Check if the employee exists
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new AppException("Employee not found with ID: " + dto.getEmployeeId()));

        // Check if the current user is the employee (employees can only create their own expenses)
        Employee currentEmployee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException("Current user is not associated with an employee record"));

        if (!dto.getEmployeeId().equals(currentEmployee.getId())) {
            throw new AppException("Employees can only create expenses for themselves");
        }

        // Create expense entity
        Expense expense = expenseMapper.toEntity(dto);
        expense.setEmployee(employee);
        expense.setStatus("PENDING"); // Default status

        // Save expense
        Expense savedExpense = expenseRepository.save(expense);

        // Log the creation
        auditLogService.log(currentUser, "CREATE_EXPENSE", "EXPENSE", savedExpense.getId(),
                "Created expense for employee: " + employee.getEmployeeId());

        // Convert to DTO
        return expenseMapper.toDto(savedExpense);
    }

    /**
     * Get expense by ID
     */
    public ExpenseDTO getExpenseById(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new AppException("Expense not found with ID: " + id));
        return expenseMapper.toDto(expense);
    }

    /**
     * Get expenses by employee ID
     */
    public java.util.List<ExpenseDTO> getExpensesByEmployeeId(UUID employeeId) {
        java.util.List<Expense> expenses = expenseRepository.findByEmployeeId(employeeId);
        return expenses.stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get expenses by status
     */
    public java.util.List<ExpenseDTO> getExpensesByStatus(String status) {
        java.util.List<Expense> expenses = expenseRepository.findByStatus(status);
        return expenses.stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get expenses by date range
     */
    public java.util.List<ExpenseDTO> getExpensesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        java.util.List<Expense> expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate);
        return expenses.stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all expenses with pagination, sorting, and filtering
     * Managers can see expenses for their department, admins can see all, employees can see their own.
     * We'll implement filtering in the service based on the current user's role.
     */
    public Page<ExpenseDTO> getAllExpenses(int page, int size, String sortBy, String direction,
                                           UUID employeeId, String status,
                                           LocalDateTime startDate, LocalDateTime endDate,
                                           User currentUser) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Base repository call - we'll get all and filter for simplicity
        Page<Expense> expensesPage = expenseRepository.findAll(pageable);

        // Apply filters
        java.util.List<Expense> filteredList = expensesPage.getContent().stream()
                .filter(e -> employeeId == null ||
                        e.getEmployee().getId().equals(employeeId))
                .filter(e -> status == null ||
                        e.getStatus().equalsIgnoreCase(status))
                .filter(e -> startDate == null ||
                        !e.getExpenseDate().isBefore(startDate))
                .filter(e -> endDate == null ||
                        !e.getExpenseDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Apply role-based filtering
        java.util.List<Expense> roleFilteredList = filteredList.stream()
                .filter(e -> {
                    // If current user is an employee, they can only see their own expenses
                    if (currentUser.getUser().Role.EMPLOYEE.equals(currentUser.getUser().getRole())) {
                        Employee currentEmployee = employeeRepository.findByUserId(currentUser.getId())
                                .orElseThrow(() -> new AppException("Current user is not associated with an employee record"));
                        return e.getEmployee().getId().equals(currentEmployee.getId());
                    }
                    // If current user is a manager, they can see expenses for their department
                    // We'll allow managers to see all for now; we'll implement proper filtering later.
                    // If current user is an admin, they can see all.
                    return true;
                })
                .collect(Collectors.toList());

        // Create a new page with filtered content
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), roleFilteredList.size());
        java.util.List<Expense> filteredPageContent = roleFilteredList.subList(start, end);

        return new PageImpl<>(filteredPageContent, pageable, roleFilteredList.size())
                .map(expenseMapper::toDto);
    }

    /**
     * Approve or reject an expense
     * Only managers can approve/reject expenses for their department employees.
     */
    public ExpenseDTO approveOrRejectExpense(UUID id, ExpenseApprovalDTO dto, User currentUser) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new AppException("Expense not found with ID: " + id));

        // Check if the expense is pending
        if (!"PENDING".equalsIgnoreCase(expense.getStatus())) {
            throw new AppException("Expense is not in pending status");
        }

        // Check if the current user is a manager or admin
        Employee currentEmployee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException("Current user is not associated with an employee record"));

        if (!currentUser.getUser().Role.MANAGER.equals(currentUser.getUser().getRole()) &&
                !currentUser.getUser().Role.ADMIN.equals(currentUser.getUser().getRole())) {
            throw new AppException("Only managers and admins can approve or reject expenses");
        }

        // TODO: Add more specific manager-department check

        // Update the expense
        if (dto.getStatus() != null) {
            expense.setStatus(dto.getStatus().toUpperCase());
        }
        if (dto.getApprovedAt() != null) {
            expense.setApprovedAt(dto.getApprovedAt());
        } else {
            expense.setApprovedAt(LocalDateTime.now());
        }
        expense.setApprovedBy(currentEmployee);

        // Save the updated expense
        Expense updatedExpense = expenseRepository.save(expense);

        // Log the action
        auditLogService.log(currentUser,
                "APPROVED".equalsIgnoreCase(dto.getStatus()) ? "APPROVE_EXPENSE" : "REJECT_EXPENSE",
                "EXPENSE", updatedExpense.getId(),
                "Expense " + dto.getStatus().toLowerCase() + " for employee: " + expense.getEmployee().getEmployeeId());

        // Convert to DTO
        return expenseMapper.toDto(updatedExpense);
    }
}