package com.team01.ticketing.error;

public class VALIDATION_ERROR extends RuntimeException {
    public VALIDATION_ERROR() {
        super("입력값을 확인해 주세요.");
    }
}
