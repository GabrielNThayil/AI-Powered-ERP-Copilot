package com.example.erp.dto.department;

import com.example.erp.entity.Department;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Department response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DepartmentDTO {

    private UUID id;
    private String name;
    private Double budget;
    private UUID managerId;
    private String managerName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}