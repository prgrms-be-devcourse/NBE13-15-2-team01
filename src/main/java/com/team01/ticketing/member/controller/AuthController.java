package com.team01.ticketing.member.controller;

import com.team01.ticketing.global.security.JwtService;
import com.team01.ticketing.member.dto.LoginRequest;
import com.team01.ticketing.member.dto.LoginResponse;
import com.team01.ticketing.member.dto.LoginResult;
import com.team01.ticketing.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    // refresh 토큰 쿠키 이름
    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    // refresh 쿠키를 /api/v1/auth 아래 요청(재발급, 로그아웃)에만 보내도록 경로 제한
    private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";

    private final AuthService authService;
    // 쿠키 유효기간(refresh 만료 시간)을 가져오려고 사용
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request);
        // 쿠키를 응답 헤더에 넣어야 해서 ResponseEntity를 사용
        // body(accessToken과 회원 정보), Set-Cookie 헤더(refreshToken)
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString()).body(result.toResponse());
    }

    // refresh 토큰을 담는 쿠키 생성 (재발급, 로그아웃에서도 재사용)
    private ResponseCookie refreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)     // JS에서 읽을 수 없게 하여 XSS로 탈취되는 것을 막음.
                .secure(false)      // 로컬은 http라서 false
                .sameSite("Strict") // 다른 사이트에서 보낸 요청에는 쿠키를 안 붙임(CSRF 방지)
                .path(REFRESH_COOKIE_PATH)
                .maxAge(jwtService.getRefreshSeconds()) // 쿠키 수명 = refresh 토큰 수명
                .build();
    }
}
