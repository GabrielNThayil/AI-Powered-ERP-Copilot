package com.example.erp.controller.employee;

import com.example.erp.dto.employee.EmployeeCreateDTO;
import com.example.erp.dto.employee.EmployeeDTO;
import com.example.erp.entity.User;
import com.example.erp.entity.Employee;
import com.example.erp.entity.Department;
import com.example.erp.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter BeforeEach;
import org.junit.jupiter Test;
import org.junit.jupiter.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test for EmployeeController
 */
@WebMvcTest(EmployeeController.class)
@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private User testUser;
    private Department testDepartment;
    private Employee testEmployee;
    private EmployeeCreateDTO createDTO;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.ADMIN);
        testUser.setStatus(User.Status.ACTIVE);

        testDepartment = new Department();
        testDepartment.setId(UUID.randomUUID());
        testDepartment.setName("Test Department");
        testDepartment.setBudget(50000.0);

        testEmployee = new Employee();
        testEmployee.setId(UUID.randomUUID());
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

        createDTO = new EmployeeCreateDTO();
        createDTO.setEmployeeId("EMP002");
        createDTO.setName("New Employee");
        createDTO.setEmail("new@example.com");
        createDTO.setPhone("0987654321");
        createDTO.setDesignation("Test Engineer");
        createDTO.setSalary(60000.0);
        createDTO.setDateOfJoining(LocalDateTime.now());
        createDTO.setDepartmentId(testDepartment.getId());
        createDTO.setEmploymentStatus("FULL_TIME");
        createDTO.setUserId(testUser.getId());

        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(testEmployee.getId());
        employeeDTO.setEmployeeId(testEmployee.getEmployeeId());
        employeeDTO.setUserId(testEmployee.getUser().getId());
        employeeDTO.setName(testEmployee.getName());
        employeeDTO.setEmail(testEmployee.getEmail());
        employeeDTO.setPhone(testEmployee.getPhone());
        employeeDTO.setDesignation(testEmployee.getDesignation());
        employeeDTO.setSalary(testEmployee.getSalary());
        employeeDTO.setDateOfJoining(testEmployee.getDateOfJoining());
        employeeDTO.setDepartmentId(testEmployee.getDepartment().getId());
        employeeDTO.setDepartmentName(testEmployee.getDepartment().getName());
        employeeDTO.setEmploymentStatus(testEmployee.getEmploymentStatus());
        employeeDTO.setCreatedAt(testEmployee.getCreatedAt());
        employeeDTO.setUpdatedAt(testEmployee.getUpdatedAt());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_Success() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(EmployeeCreateDTO.class), any(User.class))).thenReturn(employeeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Employee"))
                .andExpect(jsonPath("$.employeeId").value("EMP001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_BadRequest() throws Exception {
        // Arrange
        EmployeeCreateDTO invalidDto = new EmployeeCreateDTO();
        invalidDto.setEmployeeId(""); // invalid

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(any(UUID.class))).thenReturn(employeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Employee"))
                .andExpect(jsonPath("$.id").value(testEmployee.getId().toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(any(UUID.class))).thenThrow(new RuntimeException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeByEmployeeId_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByEmployeeId(any(String.class))).thenReturn(employeeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/employee-id/{employeeId}", testEmployee.getEmployeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("EMP001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_Success() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees(anyInt(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), anyString(), any(UUID.class), anyString(), any(User.class)))
                .thenReturn(new PageImpl<>(List.of(employeeDTO)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Employee"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_Success() throws Exception {
        // Arrange
        EmployeeUpdateDTO updateDTO = new EmployeeUpdateDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setSalary(55000.0);

        when(employeeService.updateEmployee(any(UUID.class), any(EmployeeUpdateDTO.class), any(User.class)))
                .thenReturn(employeeDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/employees/{id}", testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Employee")) // Note: we return the original DTO in mock
                .andExpect(jsonPath("$.id").value(testEmployee.getId().toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_Success() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(any(UUID.class), any(User.class));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/employees/{id}", testEmployee.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeesByDepartmentId_Success() throws Exception {
        // Arrange
        when(employeeService.getEmployeesByDepartmentId(any(UUID.class)))
                .thenReturn(List.of(employeeDTO));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/department/{departmentId}", testDepartment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Employee"));
    }
}