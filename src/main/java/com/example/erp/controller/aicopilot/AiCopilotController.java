package com.example.erp.controller.aicopilot;

import com.example.erp.dto.aicopilot.AiCopilotRequestDTO;
import com.example.erp.dto.aicopilot.AiCopilotResponseDTO;
import com.example.erp.entity.User;
import com.example.erp.service.AiCopilotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationCurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "AI Copilot", description = "AI-powered analytics and insights")
@RestController
@RequestMapping("/api/v1/copilot")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class AiCopilotController {

    private final AiCopilotService aiCopilotService;

    /**
     * Process a question and get an AI-generated response
     */
    @Operation(summary = "Ask a question to the AI Copilot", description = "Send a question to the AI Copilot and get a response based on ERP data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI-generated response",
                    content = @Content(schema = @Schema(implementation = AiCopilotResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/query")
    public ResponseEntity<AiCopilotResponseDTO> query(@Valid @RequestBody AiCopilotRequestDTO request,
                                                      @AuthenticationCurrentUser User currentUser) {
        log.info("Received question from user {}: {}", currentUser.getEmail(), request.getQuestion());
        AiCopilotResponseDTO response = aiCopilotService.processQuestion(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get suggested questions based on user role and recent activity
     * For simplicity, we'll return a static list of suggestions.
     */
    @Operation(summary = "Get suggested questions", description = "Get a list of suggested questions for the AI Copilot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of suggested questions")
    })
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@AuthenticationCurrentUser User currentUser) {
        log.info("Fetching suggested questions for user: {}", currentUser.getEmail());
        List<String> suggestions = new ArrayList<>();
        suggestions.add("How many employees do we have?");
        suggestions.add("Which department spent the most this month?");
        suggestions.add("How many pending leave requests are there?");
        suggestions.add("What was the total expense this month?");
        suggestions.add("Show the employee count per department");
        suggestions.add("How many new employees this month?");
        return ResponseEntity.ok(suggestions);
    }
}