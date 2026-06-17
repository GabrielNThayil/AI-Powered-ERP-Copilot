package com.example.erp.mapper;

import com.example.erp.dto.leave.LeaveRequestApprovalDTO;
import com.example.erp.dto.leave.LeaveRequestCreateDTO;
import com.example.erp.dto.leave.LeaveRequestDTO;
import com.example.erp.entity.LeaveRequest;
import com.example.erp.entity.Employee;
import org.mapstruct.*;

import java.util.UUID;

/**
 * Mapper for LeaveRequest entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface LeaveRequestMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "employeeEmployeeId", source = "employee.employeeId")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", source = "approvedBy.name")
    LeaveRequestDTO toDto(LeaveRequest leaveRequest);

    LeaveRequest toEntity(LeaveRequestCreateDTO dto);

    void updateEntityFromDto(LeaveRequestApprovalDTO dto, @MappingTarget LeaveRequest leaveRequest);
}