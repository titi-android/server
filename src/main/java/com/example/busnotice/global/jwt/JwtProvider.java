package com.example.busnotice.global.jwt;

import com.example.busnotice.global.security.CustomUserDetails;
import com.example.busnotice.global.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private final Long validityInSecs = 3600000L; // 1h
    private final CustomUserDetailsService customUserDetailsService;

    public String createToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInSecs);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer "를 제외하고 나머지 값만 반환
        }
        throw new IllegalArgumentException("유효하지 않은 형식의 bearer 토큰값입니다.");
    }

    public Authentication getAuthentication(String token) {
        CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(
            getUsername(token)
        );

        return new UsernamePasswordAuthenticationToken(customUserDetails,
            customUserDetails.getPassword());
    }
}
