package com.example.erp.dto.department;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for creating a Department
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DepartmentCreateDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Budget is required")
    @PositiveOrZero(message = "Budget must be positive or zero")
    private Double budget;

    @NotNull(message = "Manager ID is required")
    private UUID managerId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}