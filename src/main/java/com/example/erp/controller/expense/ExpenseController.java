package com.example.erp.controller.expense;

import com.example.erp.dto.expense.ExpenseApprovalDTO;
import com.example.erp.dto.expense.ExpenseCreateDTO;
import com.example.erp.dto.expense.ExpenseDTO;
import com.example.erp.dto.employee.EmployeeDTO;
import com.example.erp.entity.User;
import com.example.erp.service.ExpenseService;
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

@Tag(name = "Expense Management", description = "Expense management APIs")
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Create a new expense
     * Employees can create expenses for themselves
     */
    @Operation(summary = "Create a new expense", description = "Create a new expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense created",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseCreateDTO dto,
                                                    @AuthenticationCurrentUser User currentUser) {
        log.info("Creating expense for employee ID: {} by user: {}", dto.getEmployeeId(), currentUser.getEmail());
        ExpenseDTO createdExpense = expenseService.createExpense(dto, currentUser);
        return ResponseEntity.status(201).body(createdExpense);
    }

    /**
     * Get expense by ID
     */
    @Operation(summary = "Get expense by ID", description = "Get expense details by internal ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense found",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable UUID id,
                                                     @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching expense with ID: {} by user: {}", id, currentUser.getEmail());
        ExpenseDTO expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }

    /**
     * Get expenses by employee ID
     * Employees can see their own expenses, managers can see their department's, admins can see all.
     */
    @Operation(summary = "Get expenses by employee ID", description = "Get expenses for a specific employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of expenses"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<java.util.List<ExpenseDTO>> getExpensesByEmployeeId(@PathVariable UUID employeeId,
                                                                              @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching expenses for employee ID: {} by user: {}", employeeId, currentUser.getEmail());
        java.util.List<ExpenseDTO> expenses = expenseService.getExpensesByEmployeeId(employeeId);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Get expenses by status
     */
    @Operation(summary = "Get expenses by status", description = "Get expenses by their status (PENDING, APPROVED, REJECTED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of expenses"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<java.util.List<ExpenseDTO>> getExpensesByStatus(@PathVariable String status,
                                                                          @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching expenses with status: {} by user: {}", status, currentUser.getEmail());
        java.util.List<ExpenseDTO> expenses = expenseService.getExpensesByStatus(status);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Get expenses by date range
     */
    @Operation(summary = "Get expenses by date range", description = "Get expenses within a date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of expenses"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @GetMapping("/date-range")
    public ResponseEntity<java.util.List<ExpenseDTO>> getExpensesByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching expenses from {} to {} by user: {}", startDate, endDate, currentUser.getEmail());
        java.util.List<ExpenseDTO> expenses = expenseService.getExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Get all expenses with pagination, sorting, and filtering
     */
    @Operation(summary = "Get all expenses", description = "Get all expenses with pagination, sorting, and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of expenses"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping
    public ResponseEntity<Page<ExpenseDTO>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching expenses with filters by user: {}", currentUser.getEmail());
        Page<ExpenseDTO> expenses = expenseService.getAllExpenses(page, size, sortBy, direction,
                employeeId, status, startDate, endDate, currentUser);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Approve an expense
     * Only managers and admins can approve expenses
     */
    @Operation(summary = "Approve an expense", description = "Approve a pending expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense approved",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<ExpenseDTO> approveExpense(@PathVariable UUID id,
                                                     @Valid @RequestBody ExpenseApprovalDTO dto,
                                                     @AuthenticationCurrentUser User currentUser) {
        log.info("Approving expense with ID: {} by user: {}", id, currentUser.getEmail());
        // Set status to APPROVED
        dto.setStatus("APPROVED");
        ExpenseDTO approvedExpense = expenseService.approveOrRejectExpense(id, dto, currentUser);
        return ResponseEntity.ok(approvedExpense);
    }

    /**
     * Reject an expense
     * Only managers and admins can reject expenses
     */
    @Operation(summary = "Reject an expense", description = "Reject a pending expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense rejected",
                    content = @Content(schema = @Schema(implementation = ExpenseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<ExpenseDTO> rejectExpense(@PathVariable UUID id,
                                                    @Valid @RequestBody ExpenseApprovalDTO dto,
                                                    @AuthenticationCurrentUser User currentUser) {
        log.info("Rejecting expense with ID: {} by user: {}", id, currentUser.getEmail());
        // Set status to REJECTED
        dto.setStatus("REJECTED");
        ExpenseDTO rejectedExpense = expenseService.approveOrRejectExpense(id, dto, currentUser);
        return ResponseEntity.ok(rejectedExpense);
    }
}