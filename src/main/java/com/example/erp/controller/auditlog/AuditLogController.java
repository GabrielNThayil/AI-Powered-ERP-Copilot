package com.example.erp.controller.auditlog;

import com.example.erp.dto.auditlog.AuditLogDTO;
import com.example.erp.entity.User;
import com.example.erp.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationCurrentUser;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Audit Logs", description = "Audit logging APIs")
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Get audit logs with pagination, sorting, and filtering
     * Only ADMIN can access audit logs
     */
    @Operation(summary = "Get audit logs", description = "Get audit logs with pagination, sorting, and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of audit logs"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching audit logs with filters by user: {}", currentUser.getEmail());
        Page<AuditLogDTO> auditLogs = auditLogService.getAuditLogs(page, size, sortBy, direction,
                userId, action, entityType, entityId, startDate, endDate);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit log by ID
     * Only ADMIN can access audit logs
     */
    @Operation(summary = "Get audit log by ID", description = "Get audit log details by internal ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit log found",
                    content = @Content(schema = @Schema(implementation = AuditLogDTO.class))),
            @ApiResponse(responseCode = "404", description = "Audit log not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditLogDTO> getAuditLogById(@PathVariable UUID id,
                                                       @AuthenticationCurrentUser User currentUser) {
        log.info("Fetching audit log with ID: {} by user: {}", id, currentUser.getEmail());
        AuditLogDTO auditLog = auditLogService.getAuditLogById(id);
        return ResponseEntity.ok(auditLog);
    }
}