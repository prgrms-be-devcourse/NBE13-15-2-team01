package com.team01.ticketing.error;

public class TICKET_LIMIT_EXCEEDED extends RuntimeException {
    public TICKET_LIMIT_EXCEEDED() {
        super("한 회차당 최대 3매까지 예매할 수 있습니다.");
    }
}