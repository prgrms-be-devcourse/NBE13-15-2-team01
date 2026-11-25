package com.team01.ticketing.member.dto;

public final class ValidationPatterns {
    // 이메일 정규식을 하나로 관리.
    public static final String EMAIL
            = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$";
    // 비밀번호 설정 정규식을 하나로 관리. (영문, 숫자, 특수문자로 8~64자)
    public static final String PASSWORD
            = "^[A-Za-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]{8,64}$";

    private ValidationPatterns() {}

}
