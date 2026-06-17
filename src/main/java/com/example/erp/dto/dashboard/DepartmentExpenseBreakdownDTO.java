package com.example.erp.dto.dashboard;

import lombok.*;

import java.util.UUID;

/**
 * DTO for department expense breakdown
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DepartmentExpenseBreakdownDTO {

    private UUID departmentId;
    private String departmentName;
    private Double totalExpense;
}