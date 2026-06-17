package com.example.erp.dto.leave;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for approving or rejecting a leave request
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LeaveRequestApprovalDTO {

    @NotNull(message = "Status is required")
    private String status; // APPROVED or REJECTED

    private String comments; // optional comments

    private LocalDateTime approvedAt; // optional, defaults to now
}