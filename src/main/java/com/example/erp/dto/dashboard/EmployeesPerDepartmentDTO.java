package com.example.erp.dto.dashboard;

import lombok.*;

import java.util.UUID;

/**
 * DTO for employee count per department
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EmployeesPerDepartmentDTO {

    private UUID departmentId;
    private String departmentName;
    private Long employeeCount;
}