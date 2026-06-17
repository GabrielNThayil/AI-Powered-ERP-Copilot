package com.example.erp.dto.department;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for updating a Department
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DepartmentUpdateDTO {

    private String name;

    private Double budget;

    private UUID managerId;

    private String description;
}