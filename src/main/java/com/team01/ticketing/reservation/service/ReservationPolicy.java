package com.team01.ticketing.reservation.service;

import com.team01.ticketing.schedule.Schedule;
import com.team01.ticketing.schedule.ScheduleStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class ReservationPolicy {

    // 한 회원이 한 회차에 예매할 수 있는 최대 좌석 수 (ErrorCode.TICKET_LIMIT_EXCEEDED 메시지와 맞춤)
    public static final int MAX_SEATS_PER_SCHEDULE = 3;

    private final Clock clock;

    public ReservationPolicy(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    // 열린 회차 + 시작 전이어야 예매 가능 (시작 시각 정각부터 불가)
    public boolean isBookable(Schedule schedule, LocalDateTime now) {
        return schedule.getStatus() == ScheduleStatus.OPEN && now.isBefore(schedule.getStartAt());
    }
}
