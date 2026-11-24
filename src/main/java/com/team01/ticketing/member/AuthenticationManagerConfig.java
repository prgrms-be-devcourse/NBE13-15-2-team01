package com.team01.ticketing.member;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationManagerConfig {

    // 이메일과 비밀번호 확인을 맡을 AuthenticationManager를 빈으로 등록
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        // DB에서 회원을 읽고(UserDetailsService) 비밀번호를 비교하는(PasswordEncoder) 기보 구현체
        // 회원이 없을 때도 가짜 비교를 수행해서 응답 시간으로 가입 여부를 알아내기 어렵게 함.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        // 여러 Provider를 하나로 묶는 관리자. 지금은 비밀번호 로그인 Provider 하나만 등록함.
        return new ProviderManager(provider);
    }
}
