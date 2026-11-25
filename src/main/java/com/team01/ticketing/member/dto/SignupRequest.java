package com.team01.ticketing.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다.")
        @Pattern(regexp = ValidationPatterns.EMAIL,
                message = "이메일 형식을 확인해 주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Pattern(regexp = ValidationPatterns.PASSWORD,
                message = "비밀번호는 영문, 숫자, 특수문자로 8~64자여야 합니다.")
        String password,

        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
        String name
) {
}
