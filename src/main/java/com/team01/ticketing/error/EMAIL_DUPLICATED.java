package com.team01.ticketing.error;

public class EMAIL_DUPLICATED extends RuntimeException {
    public EMAIL_DUPLICATED() {
        super("이미 사용 중인 이메일입니다.");
    }
}
