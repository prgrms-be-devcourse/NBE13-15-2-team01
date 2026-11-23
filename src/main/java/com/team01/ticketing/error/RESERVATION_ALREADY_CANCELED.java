package com.team01.ticketing.error;

public class RESERVATION_ALREADY_CANCELED extends RuntimeException {
    public RESERVATION_ALREADY_CANCELED() {
        super("이미 취소된 예매입니다.");
    }
}
