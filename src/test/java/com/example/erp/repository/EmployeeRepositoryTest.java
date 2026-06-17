package com.example.erp.repository;

import com.example.erp.entity.Employee;
import com.example.erp.entity.User;
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
 * Test for EmployeeRepository custom queries
 */
@DataJpaTest
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Department testDepartment;
    private Employee testEmployee;

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

        // Create a department
        testDepartment = new Department();
        testDepartment.setName("Test Department");
        testDepartment.setBudget(50000.0);
        entityManager.persist(testDepartment);
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
        testEmployee.setDepartment(testDepartment);
        testEmployee.setEmploymentStatus("FULL_TIME");
        entityManager.persist(testEmployee);
        entityManager.flush();
    }

    @Test
    void testFindByEmployeeId() {
        Optional<Employee> result = employeeRepository.findByEmployeeId("EMP001");
        assertTrue(result.isPresent());
        assertEquals(testEmployee.getId(), result.get().getId());
    }

    @Test
    void testFindByEmail() {
        Optional<Employee> result = employeeRepository.findByEmail("employee@example.com");
        assertTrue(result.isPresent());
        assertEquals(testEmployee.getId(), result.get().getId());
    }

    @Test
    void testFindByDepartmentId() {
        List<Employee> result = employeeRepository.findByDepartmentId(testDepartment.getId());
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testEmployee.getId(), result.get(0).getId());
    }

    @Test
    void testFindByUserId() {
        Optional<Employee> result = employeeRepository.findByUserId(testUser.getId());
        assertTrue(result.isPresent());
        assertEquals(testEmployee.getId(), result.get().getId());
    }

    @Test
    void testCountEmployeesPerDepartment() {
        List<Object[]> result = employeeRepository.countEmployeesPerDepartment();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        Object[] row = result.get(0);
        assertEquals(testDepartment.getId(), row[0]);
        assertEquals(testDepartment.getName(), row[1]);
        assertEquals(1L, row[2]);
    }

    @Test
    void testCountNewEmployeesSince() {
        // Create an employee that joined recently (within the last month)
        User recentUser = new User();
        recentUser.setName("Recent User");
        recentUser.setEmail("recent@example.com");
        recentUser.setPassword("password");
        recentUser.setRole(User.Role.EMPLOYEE);
        recentUser.setStatus(User.Status.ACTIVE);
        entityManager.persist(recentUser);
        entityManager.flush();

        Department recentDept = new Department();
        recentDept.setName("Recent Dept");
        recentDept.setBudget(30000.0);
        entityManager.persist(recentDept);
        entityManager.flush();

        Employee recentEmp = new Employee();
        recentEmp.setEmployeeId("EMP002");
        recentEmp.setUser(recentUser);
        recentEmp.setName("Recent Employee");
        recentEmp.setEmail("recent@example.com");
        recentEmp.setPhone("0987654321");
        recentEmp.setDesignation("Recent Engineer");
        recentEmp.setSalary(40000.0);
        recentEmp.setDateOfJoining(LocalDateTime.now().minusDays(10)); // joined 10 days ago
        recentEmp.setDepartment(recentDept);
        recentEmp.setEmploymentStatus("FULL_TIME");
        entityManager.persist(recentEmp);
        entityManager.flush();

        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        Long count = employeeRepository.countNewEmployeesSince(startDate);
        assertEquals(1L, count); // only the recent employee
    }
}