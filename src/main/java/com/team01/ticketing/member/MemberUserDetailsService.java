package com.team01.ticketing.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DaoAuthenticationProvider가 BadCredentialsException으로 바꿔서 회원 없음과 비밀번호 틀림을 구분하지 않음.
        Member member = this.memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        String password = member.getPassword();

        // 저장된 비밀번호 해시가 비어있는 회원은 비밀번호 로그인을 할 수 없음.
        if(password == null || password.isBlank()) {
            throw new UsernameNotFoundException(email);
        }

        // 비교에 쓸 정보만 담아 돌려줌.
        // password는 BCrypt 해시이고, 실제 비교는 DaoAuthenticationProvider가 PasswordEncoder로 시행.
        // roles("USER")는 권한 이름 앞에 ROLE_을 붙여 ROLE_USER로 저장.
        return User.withUsername(member.getEmail()).password(password).roles(member.getRole().name()).build();
    }
}
