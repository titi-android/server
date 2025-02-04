package com.example.busnotice.global.jwt;

import com.example.busnotice.domain.user.RefreshToken;
import com.example.busnotice.domain.user.RefreshTokenRepository;
import com.example.busnotice.domain.user.res.RefreshTokenResponse;
import com.example.busnotice.global.code.StatusCode;
import com.example.busnotice.global.exception.JwtAuthenticationException;
import com.example.busnotice.global.exception.RefreshTokenException;
import com.example.busnotice.global.security.CustomUserDetails;
import com.example.busnotice.global.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
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
    private final RefreshTokenRepository refreshTokenRepository;

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
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("토큰이 만료되었습니다.");
        } catch (SignatureException e) {
            throw new JwtAuthenticationException("서명이 올바르지 않습니다.");
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException("토큰 형식이 올바르지 않습니다.");
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException("잘못된 JWT 토큰입니다.");
        }
    }

    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer "를 제외하고 나머지 값만 반환
        }
        throw new JwtAuthenticationException("유효하지 않은 형식의 bearer 토큰값입니다.");
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

    public RefreshTokenResponse recreateAccessToken(String refreshToken) {
        // 유저에게 등록된 리프레시 토큰인지 확인
        RefreshToken existsRefreshToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(
                () -> new RefreshTokenException(StatusCode.BAD_REQUEST,
                    "해당 유저의 리프레시 토큰이 DB에 존재하지 않습니다."));
        if (!existsRefreshToken.equals(refreshToken)) {
            new RefreshTokenException(StatusCode.BAD_REQUEST, "해당 유저에게 등록된 리프레시 토큰이 아닙니다.");
        }
        // 만료된 리프레시 토큰인지 확인
        if (isRefreshTokenExpired(refreshToken)) {
            throw new RefreshTokenException(StatusCode.BAD_REQUEST, "리프레시 토큰이 만료되었습다.");
        }
        // 해당 리프레시 토큰의 유저 정보를 통해 다시 엑세스 토큰 생성
        String accessToken = createAccessToken(existsRefreshToken.getUser().getName());
        return new RefreshTokenResponse(accessToken);
    }

}
