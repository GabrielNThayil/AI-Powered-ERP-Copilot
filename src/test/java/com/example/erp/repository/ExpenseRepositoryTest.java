package com.example.erp.repository;

import com.example.erp.entity.Expense;
import com.example.erp.entity.Employee;
com.example.erp.entity.User;
import com.example.erp.entity.Department;
import org.junit.jupiter.BeforeEach;
import org.junit.jupiter.Test;
import org.junit.jupiter.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.Assertions.*;

/**
 * Test for ExpenseRepository custom queries
 */
@DataJpaTest
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Employee testEmployee;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        // Create a user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.EMPLOYEE);
        testUser.setStatus(User.Status.ACTIVE);
        entityManager.persist(testUser);
        entityManager.flush();

        // Create an employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setUser(testUser);
        testEmployee.setName("Test Employee");
        testEmployee.setEmail("employee@example.com");
        testEmployee.setPhone("1234567890");
        testEmployee.setDesignation("Test Engineer");
        testEmployee.setSalary(50000.0);
        testEmployee.setDateOfJoining(LocalDateTime.now().minusYears(1));
        testEmployee.setEmploymentStatus("FULL_TIME");
        entityManager.persist(testEmployee);
        entityManager.flush();

        // Create a department
        testDepartment = new Department();
        testDepartment.setName("Test Department");
        testDepartment.setBudget(50000.0);
        entityManager.persist(testDepartment);
        entityManager.flush();

        // Assign employee to department
        testEmployee.setDepartment(testDepartment);
        entityManager.flush();
    }

    @Test
    void testFindByEmployeeId() {
        // Create an expense for the employee
        Expense expense = new Expense();
        expense.setEmployee(testEmployee);
        expense.setAmount(100.0);
        expense.setCategory("TRAVEL");
        expense.setDescription("Test expense");
        expense.setExpenseDate(LocalDateTime.now());
        expense.setStatus("PENDING");
        entityManager.persist(expense);
        entityManager.flush();

        List<Expense> result = expenseRepository.findByEmployeeId(testEmployee.getId());
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(expense.getId(), result.get(0).getId());
    }

    @Test
    void testFindByStatus() {
        // Create a pending expense
        Expense pendingExpense = new Expense();
        pendingExpense.setEmployee(testEmployee);
        pendingExpense.setAmount(100.0);
        pendingExpense.setCategory("TRAVEL");
        pendingExpense.setDescription("Pending expense");
        pendingExpense.setExpenseDate(LocalDateTime.now());
        pendingExpense.setStatus("PENDING");
        entityManager.persist(pendingExpense);
        entityManager.flush();

        // Create an approved expense
        Expense approvedExpense = new Expense();
        approvedExpense.setEmployee(testEmployee);
        approvedExpense.setAmount(200.0);
        approvedExpense.setCategory("FOOD");
        approvedExpense.setDescription("Approved expense");
        approvedExpense.setExpenseDate(LocalDateTime.now());
        approvedExpense.setStatus("APPROVED");
        entityManager.persist(approvedExpense);
        entityManager.flush();

        List<Expense> result = expenseRepository.findByStatus("PENDING");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    @Test
    void testFindByExpenseDateBetween() {
        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        // Create an expense within the range
        Expense expenseInRange = new Expense();
        expenseInRange.setEmployee(testEmployee);
        expenseInRange.setAmount(150.0);
        expenseInRange.setCategory("TRAVEL");
        expenseInRange.setDescription("In range expense");
        expenseInRange.setExpenseDate(LocalDateTime.now().minusDays(5));
        expenseInRange.setStatus("PENDING");
        entityManager.persist(expenseInRange);
        entityManager.flush();

        // Create an expense outside the range (too old)
        Expense expenseOld = new Expense();
        expenseOld.setEmployee(testEmployee);
        expenseOld.setAmount(50.0);
        expenseOld.setCategory("TRAVEL");
        expenseOld.setDescription("Old expense");
        expenseOld.setExpenseDate(LocalDateTime.now().minusDays(20));
        expenseOld.setStatus("PENDING");
        entityManager.persist(expenseOld);
        entityManager.flush();

        List<Expense> result = expenseRepository.findByExpenseDateBetween(start, end);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(expenseInRange.getId(), result.get(0).getId());
    }

    @Test
    void testFindByIdAndEmployeeId() {
        // Create an expense
        Expense expense = new Expense();
        expense.setEmployee(testEmployee);
        expense.setAmount(100.0);
        expense.setCategory("TRAVEL");
        expense.setDescription("Test expense");
        expense.setExpenseDate(LocalDateTime.now());
        expense.setStatus("PENDING");
        entityManager.persist(expense);
        entityManager.flush();

        Optional<Expense> result = expenseRepository.findByIdAndEmployeeId(expense.getId(), testEmployee.getId());
        assertTrue(result.isPresent());
        assertEquals(expense.getId(), result.get().getId());

        // Try with wrong employee ID
        UUID wrongEmployeeId = UUID.randomUUID();
        Optional<Expense> wrongResult = expenseRepository.findByIdAndEmployeeId(expense.getId(), wrongEmployeeId);
        assertFalse(wrongResult.isPresent());
    }

    @Test
    void testGetMonthlyExpenseTrends() {
        // Create expenses in different months
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime jan = now.withMonth(1).withDayOfMonth(15);
        LocalDateTime feb = now.withMonth(2).withDayOfMonth(15);

        Expense expenseJan = new Expense();
        expenseJan.setEmployee(testEmployee);
        expenseJan.setAmount(100.0);
        expenseJan.setCategory("TRAVEL");
        expenseJan.setDescription("January expense");
        expenseJan.setExpenseDate(jan);
        expenseJan.setStatus("PENDING");
        entityManager.persist(expenseJan);
        entityManager.flush();

        Expense expenseFeb = new Expense();
        expenseFeb.setEmployee(testEmployee);
        expenseFeb.setAmount(200.0);
        expenseFeb.setCategory("FOOD");
        expenseFeb.setDescription("February expense");
        expenseFeb.setExpenseDate(feb);
        expenseFeb.setStatus("PENDING");
        entityManager.persist(expenseFeb);
        entityManager.flush();

        LocalDateTime sixMonthsAgo = now.minusMonths(6);
        List<Object[]> result = expenseRepository.getMonthlyExpenseTrends(sixMonthsAgo);
        assertFalse(result.isEmpty());
        // We expect at least two rows (Jan and Feb)
        assertTrue(result.size() >= 2);
        // Check that the amounts are correct (we can't guarantee order, but we can check presence)
        boolean foundJan = false;
        boolean foundFeb = false;
        for (Object[] row : result) {
            LocalDateTime month = (LocalDateTime) row[0];
            Double amount = (Double) row[1];
            if (month.getYear() == jan.getYear() && month.getMonthValue() == jan.getMonthValue() && amount == 100.0) {
                foundJan = true;
            }
            if (month.getYear() == feb.getYear() && month.getMonthValue() == feb.getMonthValue() && amount == 200.0) {
                foundFeb = true;
            }
        }
        assertTrue(foundJan, "January expense not found in results");
        assertTrue(foundFeb, "February expense not found in results");
    }

    @Test
    void testGetDepartmentExpenseBreakdown() {
        // Create a second department and employee
        User user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        user2.setRole(User.Role.EMPLOYEE);
        user2.setStatus(User.Status.ACTIVE);
        entityManager.persist(user2);
        entityManager.flush();

        Employee employee2 = new Employee();
        employee2.setEmployeeId("EMP002");
        employee2.setUser(user2);
        employee2.setName("Employee Two");
        employee2.setEmail("employee2@example.com");
        employee2.setPhone("0987654321");
        employee2.setDesignation("Employee Two");
        employee2.setSalary(45000.0);
        employee2.setDateOfJoining(LocalDateTime.now().minusYears(1));
        employee2.setEmploymentStatus("FULL_TIME");
        entityManager.persist(employee2);
        entityManager.flush();

        Department dept2 = new Department();
        dept2.setName("Department Two");
        dept2.setBudget(60000.0);
        entityManager.persist(dept2);
        entityManager.flush();

        employee2.setDepartment(dept2);
        entityManager.flush();

        // Create expenses for each department
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        Expense expense1 = new Expense();
        expense1.setEmployee(testEmployee);
        expense1.setAmount(100.0);
        expense1.setCategory("TRAVEL");
        expense1.setDescription("Dept1 expense");
        expense1.setExpenseDate(startOfMonth.plusDays(5));
        expense1.setStatus("PENDING");
        entityManager.persist(expense1);
        entityManager.flush();

        Expense expense2 = new Expense();
        expense2.setEmployee(employee2);
        expense2.setAmount(200.0);
        expense2.setCategory("FOOD");
        expense2.setDescription("Dept2 expense");
        expense2.setExpenseDate(startOfMonth.plusDays(10));
        expense2.setStatus("PENDING");
        entityManager.persist(expense2);
        entityManager.flush();

        List<Object[]> result = expenseRepository.getDepartmentExpenseBreakdown(startOfMonth, endOfMonth);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        // Check that we have both departments
        boolean foundDept1 = false;
        boolean foundDept2 = false;
        for (Object[] row : result) {
            UUID deptId = (UUID) row[0];
            String deptName = (String) row[1];
            Double amount = (Double) row[2];
            if (deptId.equals(testDepartment.getId()) && deptName.equals(testDepartment.getName()) && amount == 100.0) {
                foundDept1 = true;
            }
            if (deptId.equals(dept2.getId()) && deptName.equals(dept2.getName()) && amount == 200.0) {
                foundDept2 = true;
            }
        }
        assertTrue(foundDept1, "Department One not found in breakdown");
        assertTrue(foundDept2, "Department Two not found in breakdown");
    }

    @Test
    void testGetTotalExpenseBetween() {
        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        // Create expenses within the range
        Expense expense1 = new Expense();
        expense1.setEmployee(testEmployee);
        expense1.setAmount(50.0);
        expense1.setCategory("TRAVEL");
        expense1.setDescription("Expense 1");
        expense1.setExpenseDate(LocalDateTime.now().minusDays(5));
        expense1.setStatus("PENDING");
        entityManager.persist(expense1);
        entityManager.flush();

        Expense expense2 = new Expense();
        expense2.setEmployee(testEmployee);
        expense2.setAmount(100.0);
        expense2.setCategory("FOOD");
        expense2.setDescription("Expense 2");
        expense2.setExpenseDate(LocalDateTime.now().minusDays(3));
        expense2.setStatus("PENDING");
        entityManager.persist(expense2);
        entityManager.flush();

        // Create an expense outside the range (too old)
        Expense expenseOld = new Expense();
        expenseOld.setEmployee(testEmployee);
        expenseOld.setAmount(500.0);
        expenseOld.setCategory("TRAVEL");
        expenseOld.setDescription("Old expense");
        expenseOld.setExpenseDate(LocalDateTime.now().minusDays(20));
        expenseOld.setStatus("PENDING");
        entityManager.persist(expenseOld);
        entityManager.flush();

        Double total = expenseRepository.getTotalExpenseBetween(start, end);
        assertEquals(150.0, total, 0.01); // 50 + 100 = 150
    }
}