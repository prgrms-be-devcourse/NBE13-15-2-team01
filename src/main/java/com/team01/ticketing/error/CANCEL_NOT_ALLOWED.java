package com.team01.ticketing.error;

public class CANCEL_NOT_ALLOWED extends RuntimeException {
    public CANCEL_NOT_ALLOWED() {
        super("취소할 수 없는 시간입니다.");
    }
}
