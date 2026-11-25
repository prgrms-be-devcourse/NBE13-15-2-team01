package com.team01.ticketing.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest (
        @NotBlank(message = "이메일을 입력하세요.")
        @Pattern(regexp = ValidationPatterns.EMAIL,
                message = "이메일 형식을 확인해 주세요.")
        @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력하세요.")
        // BCrypt는 72바이트까지만 처리하고, 최근 Spring Security는 72바이트 초과 시 에러를 던질 수 있음(500).
        // 회원가입에서 64자까지만 허용하므로 정상 회원은 이 제한에 걸리지 않음.
        @Size(max = 64, message = "비밀번호는 64자를 초과할 수 없습니다.")
        String password
){
}
