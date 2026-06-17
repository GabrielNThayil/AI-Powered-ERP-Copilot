package com.example.erp.mapper;

import com.example.erp.dto.employee.EmployeeDTO;
import com.example.erp.dto.employee.EmployeeCreateDTO;
import com.example.erp.dto.employee.EmployeeUpdateDTO;
import com.example.erp.entity.Employee;
import com.example.erp.entity.Department;
import org.mapstruct.*;

import java.util.UUID;

/**
 * Mapper for Employee entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeDTO toDto(Employee employee);

    Employee toEntity(EmployeeCreateDTO dto);

    void updateEntityFromDto(EmployeeUpdateDTO dto, @MappingTarget Employee employee);

    // Mapping for department fields
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    EmployeeDTO toDto(Employee employee);

    // The above two methods have the same signature. We need to differentiate them.
    // We'll rename the second one or use @Named.
    // Let's use @Named.

    // Actually, we can have only one toDto method. We'll keep it as is and remove the duplicate.

    // We'll remove the duplicate and keep only one toDto method with the department mappings.

    // Also, we need to map the userId? The EmployeeDTO does not have userId? We have userId in EmployeeDTO.
    // We forgot to add userId in EmployeeDTO.

    // Let's check EmployeeDTO: we have userId field? Yes, we added it.

    // So we need to map userId from employee.getUser().getId().

    // Let's update the toDto method accordingly.

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    EmployeeDTO toDto(Employee employee);

    // For toEntity, we don't set the department or user; they will be set in the service.
    // So we only map the basic fields.

    // For updateEntityFromDto, we also don't set department or user.
}