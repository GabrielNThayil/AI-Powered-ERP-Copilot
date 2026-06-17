package com.example.erp.mapper;

import com.example.erp.dto.expense.ExpenseApprovalDTO;
import com.example.erp.dto.expense.ExpenseCreateDTO;
import com.example.erp.dto.expense.ExpenseDTO;
import com.example.erp.entity.Expense;
import com.example.erp.entity.Employee;
import org.mapstruct.*;

import java.util.UUID;

/**
 * Mapper for Expense entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "employeeEmployeeId", source = "employee.employeeId")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", source = "approvedBy.name")
    ExpenseDTO toDto(Expense expense);

    Expense toEntity(ExpenseCreateDTO dto);

    void updateEntityFromDto(ExpenseApprovalDTO dto, @MappingTarget Expense expense);
}