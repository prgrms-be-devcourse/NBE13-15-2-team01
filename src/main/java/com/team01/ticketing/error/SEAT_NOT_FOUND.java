package com.team01.ticketing.error;

public class SEAT_NOT_FOUND extends RuntimeException {
    public SEAT_NOT_FOUND() {
        super("좌석을 찾을 수 없습니다.");
    }
}
