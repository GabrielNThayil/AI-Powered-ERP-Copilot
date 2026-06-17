package com.example.erp.dto.expense;

import com.example.erp.entity.Expense;
import com.example.erp.entity.Employee;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Expense response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ExpenseDTO {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeEmployeeId;
    private Double amount;
    private String category;
    private String description;
    private LocalDateTime expenseDate;
    private String status;
    private UUID approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}