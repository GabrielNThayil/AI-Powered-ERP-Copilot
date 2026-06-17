package com.example.erp.dto.employee;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for updating an Employee
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EmployeeUpdateDTO {

    private String employeeId;

    private String name;

    private String email;

    private String phone;

    private String designation;

    private Double salary;

    private LocalDateTime dateOfJoining;

    private UUID departmentId;

    private String employmentStatus;
}