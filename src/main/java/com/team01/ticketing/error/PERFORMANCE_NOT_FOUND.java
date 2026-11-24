package com.team01.ticketing.error;

public class PERFORMANCE_NOT_FOUND extends RuntimeException {
    public PERFORMANCE_NOT_FOUND() {
        super("공연을 찾을 수 없습니다.");
    }
}
