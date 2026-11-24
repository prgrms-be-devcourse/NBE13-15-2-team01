package com.team01.ticketing.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 서비스는 LocalDateTime.now(clock)으로 현재 시각을 얻음 → 테스트에서 Clock.fixed(...)로 교체
    @Bean
    public Clock clock() {
        return Clock.system(SEOUL);
    }
}
