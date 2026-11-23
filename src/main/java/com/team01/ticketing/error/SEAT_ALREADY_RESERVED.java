package com.team01.ticketing.error;

public class SEAT_ALREADY_RESERVED extends RuntimeException {
    public SEAT_ALREADY_RESERVED() {
        super("이미 예매된 좌석입니다.");
    }
}
