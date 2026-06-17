package com.example.erp.security.filter;

import com.example.erp.security.service.CustomUserDetailsService;
import com.example.erp.security.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Component;
    import org.springframework.web.filter.OncePerRequestFilter;

    import java.io.IOException;

    /**
     * Filter to validate JWT tokens in request headers
     */
    @Component
    @RequiredArgsConstructor
    @Slf4j
    public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtTokenUtil jwtTokenUtil;
        private final CustomUserDetailsService userDetailsService;

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            // Get token from header
            String token = jwtTokenUtil.resolveToken(request);
            String username = null;

            if (token != null) {
                try {
                    username = jwtTokenUtil.extractUsername(token);
                } catch (IllegalArgumentException e) {
                    log.error("Unable to get JWT Token", e);
                } catch (ExpiredJwtException e) {
                    log.error("JWT Token has expired", e);
                }
            } else {
                logger.debug("No JWT token found in request");
            }

            // Validate token if we have a username and no existing authentication
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtTokenUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(authenticationDetailsSource.buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    log.debug("User {} authenticated successfully", username);
                } else {
                    log.debug("JWT token validation failed for user: {}", username);
                }
            }

            filterChain.doFilter(request, response);
        }
    }