package com.team01.ticketing.member.service;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;
import com.team01.ticketing.global.security.JwtService;
import com.team01.ticketing.member.dto.LoginRequest;
import com.team01.ticketing.member.dto.LoginResult;
import com.team01.ticketing.member.dto.MemberResponse;
import com.team01.ticketing.member.entity.Member;
import com.team01.ticketing.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// 로그인, 토큰 재발급, 로그아웃의 처리 순서를 담당. (토큰 발급 및 검증과 같은 인증 흐름)
@Service
@RequiredArgsConstructor
public class AuthService {

    // 이메일, 비밀번호 확인
    private final PasswordAuthenticationService passwordAuthenticationService;
    // 회원 id, role 조회
    private final MemberRepository memberRepository;
    // 토큰 발급
    private final JwtService jwtService;

    // 로그인 : 비밀번호 확인 -> 회원 조회 -> access, refresh 토큰 발급
    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        // 1. 이메일, 비밀번호 확인 (실패하면 여기서 LOGIN_FAILED(401)이 던져짐)
        String email = passwordAuthenticationService.authenticateEmail(request);
        // 2. 토큰에 넣을 id와 role 확인을 위해 회원 조회
        // 인증 성공 후 회원이 삭제된 경우를 대비하여 같은 LOGIN_FAILED(401)로 처리
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        // 3. access(API 호출용), refresh(재발급용)
        String accessToken = jwtService.createAccess(member.getId(), member.getRole());
        String refreshToken = jwtService.createRefresh(member.getId());
        // 4. 컨트롤러가 access와 회원 정보는 body로, refresh는 쿠키로 나눠서 보냄.
        return new LoginResult(accessToken, refreshToken, MemberResponse.from(member));
    }
}
