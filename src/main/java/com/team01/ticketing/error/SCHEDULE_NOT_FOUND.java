package com.team01.ticketing.error;

public class SCHEDULE_NOT_FOUND extends RuntimeException {
    public SCHEDULE_NOT_FOUND() {
        super("회차를 찾을 수 없습니다.");
    }
}
