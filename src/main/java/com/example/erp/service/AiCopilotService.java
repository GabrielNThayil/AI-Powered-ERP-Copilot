package com.example.erp.service;

import com.example.erp.dto.aicopilot.AiCopilotRequestDTO;
import com.example.erp.dto.aicopilot.AiCopilotResponseDTO;
import com.example.erp.entity.*;
import com.example.erp.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for AI Copilot integration with Gemini API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiCopilotService {

    private final WebClient webClient;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ExpenseRepository expenseRepository;

    @Value("${app.gemini-api-key}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Process a question and return an AI-generated response
     */
    public AiCopilotResponseDTO processQuestion(AiCopilotRequestDTO request) {
        String question = request.getQuestion().toLowerCase().trim();
        log.info("Processing question: {}", question);

        // Analyze the question to determine what data is needed
        String answer;
        List<String> suggestions = new ArrayList<>();

        // Simple keyword-based analysis (in a real app, use NLP)
        if (question.contains("department") && question.contains("spent") && question.contains("most")) {
            // Which department spent the most this month?
            answer = getDepartmentWithHighestExpenseThisMonth();
            suggestions.add("Show the expense breakdown by department");
            suggestions.add("What was the total expense this month?");
        } else if (question.contains("total") && question.contains("expense") && question.contains("this month")) {
            // What was the total expense this month?
            answer = getTotalExpenseThisMonth();
            suggestions.add("Which department spent the most this month?");
            suggestions.add("Show the expense breakdown by department");
        } else if (question.contains("pending") && question.contains("leave")) {
            // How many pending leave requests?
            long pendingLeaves = leaveRequestRepository.countByStatus("PENDING");
            answer = "There are " + pendingLeaves + " pending leave requests.";
            suggestions.add("Show the list of pending leave requests");
            suggestions.add("How many approved leave requests are there?");
        } else if (question.contains("approved") && question.contains("leave")) {
            // How many approved leave requests?
            long approvedLeaves = leaveRequestRepository.countByStatus("APPROVED");
            answer = "There are " + approvedLeaves + " approved leave requests.";
            suggestions.add("Show the list of approved leave requests");
            suggestions.add("How many pending leave requests are there?");
        } else if (question.contains("new employees") && question.contains("this month")) {
            // How many new employees this month?
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long newEmployees = employeeRepository.countNewEmployeesSince(startOfMonth);
            answer = "There are " + newEmployees + " new employees this month.";
            suggestions.add("Show the list of new employees this month");
            suggestions.add("How many employees do we have in total?");
        } else if (question.contains("total employees")) {
            // How many employees do we have in total?
            long totalEmployees = employeeRepository.count();
            answer = "We have " + totalEmployees + " employees in total.";
            suggestions.add("How many new employees this month?");
            suggestions.add("Show the employee count per department");
        } else if (question.contains("employees per department")) {
            // Show employee count per department
            List<Object[]> results = employeeRepository.countEmployeesPerDepartment();
            StringBuilder sb = new StringBuilder();
            sb.append("Employee count per department:\n");
            for (Object[] row : results) {
                String deptName = (String) row[1];
                Long count = ((Number) row[2]).longValue();
                sb.append("- ").append(deptName).append(": ").append(count).append("\n");
            }
            answer = sb.toString();
            suggestions.add("Which department has the most employees?");
            suggestions.add("Show the employee list for a specific department");
        } else if (question.contains("hello") || question.contains("hi")) {
            answer = "Hello! I'm your AI Copilot. I can help you with questions about employees, departments, leaves, and expenses. Try asking:\n" +
                    "- How many employees do we have?\n" +
                    "- Which department spent the most this month?\n" +
                    "- How many pending leave requests are there?";
            suggestions.add("How many employees do we have?");
            suggestions.add("Which department spent the most this month?");
            suggestions.add("How many pending leave requests are there?");
        } else {
            // Default response: use Gemini to generate a answer based on general knowledge
            // For now, we'll return a generic response
            answer = "I'm sorry, I don't have enough information to answer that question. Please try rephrasing or ask about employees, departments, leaves, or expenses.";
            suggestions.add("How many employees do we have?");
            suggestions.add("How many pending leave requests are there?");
            suggestions.add("What was the total expense this month?");
        }

        return AiCopilotResponseDTO.builder()
                .answer(answer)
                .suggestions(suggestions.toArray(new String[0]))
                .build();
    }

    /**
     * Get the department with the highest expense this month
     */
    private String getDepartmentWithHighestExpenseThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        List<Object[]> results = expenseRepository.getDepartmentExpenseBreakdown(startOfMonth, endOfMonth);
        if (results.isEmpty()) {
            return "No expenses recorded this month.";
        }

        // Find the department with the highest total expense
        Optional<Object[]> max = results.stream()
                .max(Comparator.comparingDouble(o -> ((Number) o[2]).doubleValue()));

        if (max.isPresent()) {
            Object[] row = max.get();
            String departmentName = (String) row[1];
            Double totalExpense = ((Number) row[2]).doubleValue();
            return String.format("The %s department spent the most this month with a total of %.2f.", departmentName, totalExpense);
        } else {
            return "Unable to determine the department with the highest expense.";
        }
    }

    /**
     * Get the total expense this month
     */
    private String getTotalExpenseThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        Double total = expenseRepository.getTotalExpenseBetween(startOfMonth, endOfMonth);
        if (total == null) {
            total = 0.0;
        }
        return "The total expense this month is " + String.format("%.2f", total) + ".";
    }

    /**
     * Call Gemini API to generate a response based on a prompt
     * This is a fallback for complex questions
     */
    private String callGeminiApi(String prompt) {
        // Build the request body for Gemini API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", prompt)
                ))
        ));

        // Make the API call
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + geminiApiKey;
        ResponseEntity<String> response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .toEntity(String.class)
                .block();

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode firstCandidate = candidates.get(0);
                    JsonNode content = firstCandidate.path("content");
                    JsonNode parts = content.path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        JsonNode firstPart = parts.get(0);
                        return firstPart.path("text").asText();
                    }
                }
            } catch (Exception e) {
                log.error("Error parsing Gemini API response", e);
                return "Error processing the request with Gemini API.";
            }
        } else {
            log.error("Gemini API returned status: {}", response.getStatusCode());
            return "Error calling Gemini API.";
        }

        return "No response from Gemini API.";
    }
}