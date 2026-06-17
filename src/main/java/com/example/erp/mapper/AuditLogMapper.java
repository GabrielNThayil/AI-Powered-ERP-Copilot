package com.example.erp.mapper;

import com.example.erp.dto.auditlog.AuditLogDTO;
import com.example.erp.entity.AuditLog;
import com.example.erp.entity.User;
import org.mapstruct.*;

import java.util.UUID;

/**
 * Mapper for AuditLog entity and DTO
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userEmail", source = "user.email")
    AuditLogDTO toDto(AuditLog auditLog);
}