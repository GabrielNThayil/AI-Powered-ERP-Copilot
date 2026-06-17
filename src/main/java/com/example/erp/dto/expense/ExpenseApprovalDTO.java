package com.example.erp.dto.expense;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for approving or rejecting an Expense
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ExpenseApprovalDTO {

    @NotNull(message = "Status is required")
    private String status; // APPROVED or REJECTED

    private String comments; // optional comments

    private LocalDateTime approvedAt; // optional, defaults to now
}