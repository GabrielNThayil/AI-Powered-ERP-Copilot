package com.example.erp.dto.dashboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SummaryDTO {

    private Long totalEmployees;
    private Long totalDepartments;
    private Long totalPendingLeaves;
    private Long totalApprovedLeaves;
    private Long totalPendingExpenses;
    private Long totalApprovedExpenses;
}