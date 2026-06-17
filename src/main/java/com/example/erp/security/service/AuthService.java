package com.example.erp.security.service;

import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.UserRepository;
import com.example.erp.security.dto.AuthenticationRequestDTO;
import com.example.erp.security.dto.AuthenticationResponseDTO;
import com.example.erp.security.dto.RegisterRequestDTO;
import com.example.erp.security.dto.UserDTO;
import com.example.erp.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Service handling authentication operations: login, registration, token refresh
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Authenticate user and generate JWT token
     */
    public AuthenticationResponseDTO login(AuthenticationRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Load user details to get user info
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AppException("User not found after authentication"));

            String token = jwtTokenUtil.generateToken(user.getEmail());
            long expiresIn = jwtTokenUtil.getJwtExpirationMs();

            return AuthenticationResponseDTO.builder()
                    .accessToken(token)
                    .expiresIn(expiresIn)
                    .userId(user.getId().toString())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .build();

        } catch (BadCredentialsException ex) {
            log.error("Invalid credentials for email: {}", request.getEmail());
            throw new AppException("Invalid email or password");
        }
    }

    /**
     * Register a new user
     */
    public UserDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email address already in use.");
        }

        // Create user entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.EMPLOYEE); // Default role for self-registration
        user.setStatus(User.Status.ACTIVE);

        User savedUser = userRepository.save(user);

        log.info("New user registered: {}", savedUser.getEmail());

        return UserDTO.builder()
                .id(savedUser.getId().toString())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    /**
     * Refresh JWT token
     */
    public AuthenticationResponseDTO refreshToken(String refreshToken) {
        String username = jwtTokenUtil.extractUsername(refreshToken);
        if (username == null || jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new AppException("Invalid or expired refresh token");
        }

        // Generate new token
        String token = jwtTokenUtil.generateToken(new HashMap<>(), username);
        long expiresIn = jwtTokenUtil.getJwtExpirationMs();

        // Fetch user info for response
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException("User not found"));

        return AuthenticationResponseDTO.builder()
                .accessToken(token)
                .expiresIn(expiresIn)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}