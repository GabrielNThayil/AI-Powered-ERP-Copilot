package com.example.erp.service;

import com.example.erp.dto.auditlog.AuditLogDTO;
import com.example.erp.entity.AuditLog;
import com.example.erp.entity.User;
import com.example.erp.mapper.AuditLogMapper;
import com.example.erp.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for audit logging operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    /**
     * Log an audit event
     */
    public void log(User user, String action, String entityType, String entityId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
        log.info("Audit log created: {} {}", action, entityType);
    }

    /**
     * Log an audit event with entity ID as UUID
     */
    public void log(User user, String action, String entityType, UUID entityId, String details) {
        log(user, action, action, entityType, entityId.toString(), details);
    }

    /**
     * Get audit logs with pagination, sorting, and filtering
     * Only for admin users (we'll enforce in controller)
     */
    public Page<AuditLogDTO> getAuditLogs(int page, int size, String sortBy, String direction,
                                          UUID userId, String action,
                                          String entityType, String entityId,
                                          LocalDateTime startDate, LocalDateTime endDate) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // We'll get all and filter for simplicity (not efficient for large data)
        Page<AuditLog> auditLogsPage = auditLogRepository.findAll(pageable);

        // Apply filters
        java.util.List<AuditLog> filteredList = auditLogsPage.getContent().stream()
                .filter(log -> userId == null ||
                        (log.getUser() != null && log.getUser().getId().equals(userId)))
                .filter(log -> action == null ||
                        log.getAction().equalsIgnoreCase(action))
                .filter(log -> entityType == null ||
                        log.getEntityType().equalsIgnoreCase(entityType))
                .filter(log -> entityId == null ||
                        log.getEntityId().equals(entityId))
                .filter(log -> startDate == null ||
                        !log.getTimestamp().isBefore(startDate))
                .filter(log -> endDate == null ||
                        !log.getTimestamp().isAfter(endDate))
                .collect(Collectors.toList());

        // Create a new page with filtered content
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        java.util.List<AuditLog> filteredPageContent = filteredList.subList(start, end);

        // Convert to DTOs
        java.util.List<AuditLogDTO> dtos = filteredPageContent.stream()
                .map(auditLogMapper::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, filteredList.size());
    }

    /**
     * Get audit log by ID
     */
    public AuditLogDTO getAuditLogById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Audit log not found with ID: " + id));
        return auditLogMapper.toDto(auditLog);
    }
}