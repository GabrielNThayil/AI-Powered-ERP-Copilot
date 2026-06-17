package com.example.erp.repository;

import com.example.erp.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Expense entity
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByEmployeeId(UUID employeeId);

    List<Expense> findByStatus(String status);

    List<Expense> findByExpenseDateBetween(LocalDateTime start, LocalDateTime end);

    Optional<Expense> findByIdAndEmployeeId(UUID id, UUID employeeId);

    Long countByStatus(String status);

    @Query("SELECT DATE_TRUNC('month', e.expenseDate) as month, SUM(e.amount) " +
           "FROM Expense e " +
           "WHERE e.expenseDate >= :startDate " +
           "GROUP BY DATE_TRUNC('month', e.expenseDate) " +
           "ORDER BY month")
    List<Object[]> getMonthlyExpenseTrends(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT e.department.id, e.department.name, SUM(e.amount) " +
           "FROM Expense e " +
           "WHERE e.expenseDate >= :startDate AND e.expenseDate <= :endDate " +
           "GROUP BY e.department.id, e.department.name")
    List<Object[]> getDepartmentExpenseBreakdown(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e " +
           "WHERE e.expenseDate >= :startDate AND e.expenseDate <= :endDate")
    Double getTotalExpenseBetween(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
}