package com.example.erp.dto.leave;

import com.example.erp.entity.LeaveRequest;
import com.example.erp.entity.Employee;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for LeaveRequest response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LeaveRequestDTO {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeEmployeeId;
    private String leaveType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reason;
    private String status;
    private UUID approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}