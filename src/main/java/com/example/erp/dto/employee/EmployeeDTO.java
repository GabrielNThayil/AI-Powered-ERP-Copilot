package com.example.erp.dto.employee;

import com.example.erp.entity.Employee;
import com.example.erp.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Employee response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EmployeeDTO {

    private UUID id;
    private String employeeId;
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private String designation;
    private Double salary;
    private LocalDateTime dateOfJoining;
    private UUID departmentId;
    private String departmentName;
    private String employmentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}