package com.team01.ticketing.error;

public class RESERVATION_NOT_FOUND extends RuntimeException {
    public RESERVATION_NOT_FOUND() {
        super("예매를 찾을 수 없습니다.");
    }
}
