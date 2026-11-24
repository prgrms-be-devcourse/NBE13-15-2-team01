package com.team01.ticketing.error;

public class NOT_RESERVATION_OWNER extends RuntimeException {
    public NOT_RESERVATION_OWNER() {
        super("본인의 예매가 아닙니다.");
    }
}
