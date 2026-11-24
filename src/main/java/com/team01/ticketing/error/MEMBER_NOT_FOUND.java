package com.team01.ticketing.error;

public class MEMBER_NOT_FOUND extends RuntimeException {
    public MEMBER_NOT_FOUND() {
        super("회원을 찾을 수 없습니다.");
    }
}
