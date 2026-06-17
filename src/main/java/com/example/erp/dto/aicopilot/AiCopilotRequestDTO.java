package com.example.erp.dto.aicopilot;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AiCopilotRequestDTO {

    @NotBlank(message = "Question is required")
    private String question;
}