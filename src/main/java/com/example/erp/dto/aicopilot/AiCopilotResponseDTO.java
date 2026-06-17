package com.example.erp.dto.aicopilot;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AiCopilotResponseDTO {

    private String answer;
    private String[] suggestions; // optional follow-up questions
}