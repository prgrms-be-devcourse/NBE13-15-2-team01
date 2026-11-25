package com.team01.ticketing.member.service;

import com.team01.ticketing.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


// 로그인, 토큰 재발급, 로그아웃을 처리 순서를 담당. (토큰 발급 및 검증과 같은 인증 흐름)
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;


}
