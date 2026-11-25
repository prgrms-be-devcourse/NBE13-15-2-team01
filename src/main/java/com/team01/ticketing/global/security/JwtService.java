package com.team01.ticketing.global.security;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;
import com.team01.ticketing.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

// JWT 발급과 검증을 담당하는 빈.
// 토큰 관련 실패는 모두 UNAUTHORIZED(401)로 통일.
@Component
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String CLAIM_TYPE = "tokenType";
    public static final String CLAIM_ROLE = "role";
    // Authorization 헤더 값의 접두어
    private static final String BEARER_PREFIX = "Bearer ";
    // 서명과 검증에 쓰는 비밀 키
    private final SecretKey key;
    private final long accessSeconds;
    private final long refreshSeconds;

    // application.yml의 해당 설정값을 주입받음.
    // secret은 .env의 JWT_SECRET에서 옴. 값이 없으면 앱 시작 시 오류.
    public JwtService(
            @Value("${ticketing.jwt.secret}") String secret,
            @Value("${ticketing.jwt.access-seconds}") long accessSeconds,
            @Value("${ticketing.jwt.refresh-seconds}") long refreshSeconds) {

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessSeconds = accessSeconds;
        this.refreshSeconds = refreshSeconds;
    }

    public String createAccess(Long memberId, Role role) {
        Instant now = Instant.now();
        return Jwts.builder()
                // sub: 회원 식별자 (바뀔 수 있는 email 대신 바뀌지 않는 id 사용)
                .subject(String.valueOf(memberId))
                // jti: 토큰마다 다른 고유값. 같은 초에 발급해도 토큰 문자열이 달라짐.
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessSeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // refresh 토큰 발급 (memberId만 담고, 재발급할 때 DB에서 최신 role을 다시 읽음.)
    public String createRefresh(Long memberId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshSeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // access 검증 (JWT 필터에서 사용)
    public Claims parseAccess(String token) {
        return parseWithType(token, TYPE_ACCESS);
    }

    // refresh 검증 (토큰 재발급에서 사용)
    public Claims parseRefresh(String token) {
        return parseWithType(token, TYPE_REFRESH);
    }

    // 검증된 클레임의 sub를 Long memberId로 변환. (숫자가 아니면 401)
    public Long getMemberId(Claims claims) {
        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    // 검증된 access 클레임에서 권한을 enum으로 꺼냄. (없거나 모르는 값이면 401)
    public Role getRole(Claims claims) {
        String role = claims.get(CLAIM_ROLE, String.class);
        if (role == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    // 컨트롤러가 refresh 쿠키의 maxAge를 토큰 수명과 맞출 때 사용.
    public long getRefreshSeconds() {
        return refreshSeconds;
    }

    // 헤더에서 토큰만 꺼냄. (JWT 필터에서 사용.)
    public String bearerValue(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return token;
    }

    // 클레임을 믿기 전에 서명, 만료, 용도, 필수 항목을 모두 확인.
    private Claims parseWithType(String token, String expectedType) {
        // 빈 값은 파서에 넘기기 전에 거절.
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            // 서명과 만료를 검증한 뒤에만 payload를 꺼냄.
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            // 용도가 다르면 거절. (refresh로 API를 호출하거나 access로 재발급하는 것을 막음.)
            if (!expectedType.equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            // 서명이 맞아도 회원 식별자나 만료 시각이 없으면 받지 않음.
            if (claims.getSubject() == null
                || claims.getSubject().isBlank()
                || claims.getExpiration() == null) {

                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            return claims;
        } catch (JwtException | IllegalArgumentException ex) {
            // 만료, 서명 불일치, 형식 오류를 모두 401로 변환.
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

}
