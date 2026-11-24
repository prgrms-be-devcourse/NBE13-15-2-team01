package com.team01.ticketing.error;

public class LOGIN_FAILED extends RuntimeException {
    public LOGIN_FAILED() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
