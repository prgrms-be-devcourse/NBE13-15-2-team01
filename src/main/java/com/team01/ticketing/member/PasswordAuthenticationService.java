package com.team01.ticketing.member;

import com.team01.ticketing.error.LOGIN_FAILED;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PasswordAuthenticationService {

    private final AuthenticationManager authenticationManager;

    // 확인에 성공하면 인증된 이메일을 반환하고, 실패하면 LOGIN_FAILED(401 에러)를 던짐.
    public String authenticateEmail(LoginRequest request) {
        // 컨트롤러의 @Valid를 거치지 않고 호출되는 경우까지 대비해 한 번 더 막음.
        // 한글 비밀번호는 1글자가 3바이트라서 글자 수가 아니라 바이트 수로 검사.
        if (request.email() == null
            || request.email().isBlank()
            || request.password() == null
            || request.password().isBlank()
            || request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new LOGIN_FAILED();
        }
        // 아직 인증되지 않은 입력값(이메일, 평문 비밀번호)을 Security 형식으로 감싸기
        Authentication input = UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password());

        // AuthenticationManager가 회원 조회 후 비밀번호 비교를 수행.
        // 성공하면 인증된 Authentication이 반환, getName()은 UserDetails의 username(email).
        try {
            return authenticationManager.authenticate(input).getName();
        // 회원 없음과 비밀번호 틀림을 같은 예외로 바꿔서 가입된 이메일인지 응답으로 알 수 없게 함.
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            throw new LOGIN_FAILED();
        }
    }
}
