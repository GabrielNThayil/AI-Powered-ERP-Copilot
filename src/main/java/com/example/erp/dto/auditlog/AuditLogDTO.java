package com.example.erp.dto.auditlog;

import com.example.erp.entity.AuditLog;
import com.example.erp.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for AuditLog response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AuditLogDTO {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private LocalDateTime timestamp;
}