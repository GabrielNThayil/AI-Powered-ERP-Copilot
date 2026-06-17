package com.example.erp.mapper;

import com.example.erp.dto.department.DepartmentDTO;
import com.example.erp.dto.department.DepartmentCreateDTO;
import com.example.erp.dto.department.DepartmentUpdateDTO;
import com.example.erp.entity.Department;
import com.example.erp.entity.Employee;
import org.mapstruct.*;

import java.util.UUID;

/**
 * Mapper for Department entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", source = "manager.name")
    DepartmentDTO toDto(Department department);

    Department toEntity(DepartmentCreateDTO dto);

    void updateEntityFromDto(DepartmentUpdateDTO dto, @MappingTarget Department department);
}