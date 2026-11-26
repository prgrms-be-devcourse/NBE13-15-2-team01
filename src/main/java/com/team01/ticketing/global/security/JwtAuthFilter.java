package com.team01.ticketing.global.security;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.member.entity.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 요청마다 한 번 실행되어 access 토큰을 확인하고, 유효하면 회원 id와 권한을 SecurityContext에 저장
// @Component를 붙이지 않고 SecurityConfig에서 new로 생성해 등록 (서블릿 필터로 중복 등록 방지)
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Authorization 헤더 값 : "Bearer xxx"
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        // Bearer 토큰이 있을 때만 검사 (없으면 비로그인 상태로 다음 필터로 넘어감)
        // 막을지 말지는 SecurityConfig 규칙이 결정 (보호된 API면 EntryPoint가 401 응답)
        if (header != null && header.startsWith("Bearer ")) {
            try {
                // "Bearer " 뒤의 토큰만 꺼냄
                String token = this.jwtService.bearerValue(header);
                // 서명, 만료, access 토큰인지 검증 후 회원 id와 권한을 꺼냄 (DB 조회 없음)
                Claims claims = this.jwtService.parseAccess(token);
                Long memberId = this.jwtService.getMemberId(claims);
                Role role = this.jwtService.getRole(claims);
                // 인증 완료 객체 생성
                // principal : memberId (컨트롤러에서 @AuthenticationPrincipal Long memberId로 사용)
                // 권한 : hasRole("ADMIN")은 ROLE_ADMIN을 찾으므로 "ROLE_"을 붙임
                UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken
                        .authenticated(memberId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
                // 이번 요청 전용 SecurityContext를 만들어 인증 정보를 저장
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (BusinessException ex) {
                // 만료, 변조, refresh 토큰 등 잘못된 토큰이면 인증 정보를 비우고 비로그인으로 진행
                // 필터 예외는 @RestControllerAdvice가 잡지 못하므로 여기서 처리
                SecurityContextHolder.clearContext();
            }
        }
        // 다음 필터로 요청을 넘김 (항상 호출해야 컨트롤러까지 감)
        filterChain.doFilter(request, response);
    }
}
