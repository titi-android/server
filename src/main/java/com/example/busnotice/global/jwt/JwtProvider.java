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

    private final Long accessValidityInSecs = 86400000L; // 1일 (24시간)
    private final Long refreshValidityInSecs = 7L * 86400000L; // 7일 (1주일)

    private final CustomUserDetailsService customUserDetailsService;

    // 엑세스 토큰 발급
    public String createAccessToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessValidityInSecs);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    // 리프레시 토큰 발급
    public String createRefreshToken() {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshValidityInSecs);

        return Jwts.builder()
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

    public boolean isRefreshTokenExpired(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey) // 서명 키 설정
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

            Date expiration = claims.getExpiration();
            return expiration.before(new Date()); // 만료 시간이 현재 시간보다 이전이면 만료됨
        } catch (Exception e) {
            return true; // 파싱 오류가 발생하면 만료된 것으로 간주
        }
    }

//    public String recreateAccessToken(String refreshToken) {
//        boolean isRefreshTokenExpired = isRefreshTokenExpired(refreshToken);
//        if(isRefreshTokenExpired){
//            throw new RefreshTokenException(StatusCode.BAD_REQUEST, "리프레시 토큰이 만료되었스니다.");
//        }
//        user
//    }
}
