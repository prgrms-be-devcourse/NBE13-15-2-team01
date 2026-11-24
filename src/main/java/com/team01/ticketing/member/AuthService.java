package com.team01.ticketing.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 회원가입, 로그인, 토큰 재발급의 처리 순서를 담당하는 서비스.
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordAuthenticationService passwordAuthenticationService;
    //private final JwtService jwtSercie;
}
