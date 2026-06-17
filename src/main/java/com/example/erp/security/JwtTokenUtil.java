package com.example.erp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Component;

    import java.security.Key;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.function.Function;

    @Component
    @Slf4j
    public class JwtTokenUtil {

        @Value("${app.jwt-secret}")
        private String secret;

        @Value("${app.jwt-expiration-ms}")
        private long jwtExpirationMs;

        /**
         * Generate token for given user details
         */
        public String generateToken(UserDetails userDetails) {
            Map<String, Object> claims = new HashMap<>();
            return doGenerateToken(claims, userDetails.getUsername());
        }

        /**
         * Generate token with additional claims
         */
        public String generateToken(Map<String, Object> claims, String subject) {
            return doGenerateToken(claims, subject);
        }

        /**
         * Generate token for given subject (username) with no additional claims
         */
        public String generateToken(String subject) {
            return doGenerateToken(new HashMap<>(), subject);
        }

        private String doGenerateToken(Map<String, Object> claims, String subject) {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact();
        }

        /**
         * Validate token and check if it's expired
         */
        public boolean validateToken(String token, UserDetails userDetails) {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        }

        /**
         * Extract username from token
         */
        public String extractUsername(String token) {
            return extractClaim(token, Claims::getSubject);
        }

        /**
         * Extract expiration date from token
         */
        public Date extractExpiration(String token) {
            return extractClaim(token, Claims::getExpiration);
        }

        /**
         * Check if token is expired
         */
        public boolean isTokenExpired(String token) {
            return extractExpiration(token).before(new Date());
        }

        /**
         * Extract claim of type T from token
         */
        public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        }

        /**
         * Extract all claims from token
         */
        private Claims extractAllClaims(String token) {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }

        /**
         * Get signing key from secret
         */
        private Key getSignKey() {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        }

        /**
         * Extract token from HTTP request header
         */
        public String resolveToken(HttpServletRequest request) {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            return null;
        }

        /**
         * Get JWT expiration time in milliseconds
         */
        public long getJwtExpirationMs() {
            return jwtExpirationMs;
        }
    }