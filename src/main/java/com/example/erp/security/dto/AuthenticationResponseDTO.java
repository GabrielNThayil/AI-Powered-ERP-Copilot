package com.example.erp.security.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponseDTO {

    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private String userId;
    private String email;
    private String name;
    private String role;
}