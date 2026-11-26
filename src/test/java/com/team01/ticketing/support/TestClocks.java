package com.team01.ticketing.support;

import com.team01.ticketing.global.config.ClockConfig;

import java.time.Clock;
import java.time.LocalDateTime;

// 테스트용 고정 시계: 서울 시각을 그대로 넣으면 그 시각에 멈춘 Clock을 돌려줌
public final class TestClocks {

    private TestClocks() {
    }

    public static Clock fixedAt(LocalDateTime seoulTime) {
        return Clock.fixed(seoulTime.atZone(ClockConfig.SEOUL).toInstant(), ClockConfig.SEOUL);
    }
}
