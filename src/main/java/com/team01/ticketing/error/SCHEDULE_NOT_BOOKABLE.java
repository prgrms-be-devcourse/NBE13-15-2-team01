package com.team01.ticketing.error;

public class SCHEDULE_NOT_BOOKABLE extends RuntimeException {
    public SCHEDULE_NOT_BOOKABLE() {
        super("예매할 수 없는 회차입니다.");
    }
}
