package com.example.erp.dto.dashboard;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for monthly expense trends
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MonthlyExpenseTrendsDTO {

    private LocalDateTime month; // first day of the month
    private Double totalExpense;
}