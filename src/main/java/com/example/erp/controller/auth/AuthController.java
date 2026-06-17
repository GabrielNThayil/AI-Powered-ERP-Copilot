package com.example.erp.controller.auth;

import com.example.erp.dto.ApiResponse;
import com.example.erp.security.dto.AuthenticationRequestDTO;
import com.example.erp.security.dto.AuthenticationResponseDTO;
import com.example.erp.security.dto.RegisterRequestDTO;
import com.example.erp.security.dto.UserDTO;
import com.example.erp.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication operations: login, registration, token refresh
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@Valid @RequestBody AuthenticationRequestDTO request) {
        log.info("Login attempt for email: {}", request.getEmail());
        AuthenticationResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Register endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("Register attempt for email: {}", request.getEmail());
        UserDTO response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponseDTO> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        log.info("Token refresh request");
        // Remove "Bearer " prefix if present
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        AuthenticationResponseDTO response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}