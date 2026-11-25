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

    // 회차 시작 며칠 전 00:00부터 예매를 여는지
    private static final int BOOKING_OPEN_DAYS_BEFORE = 7;

    private final Clock clock;

    public ReservationPolicy(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    // 열린 회차 + 예매 오픈 이후 + 시작 전이어야 예매 가능 (오픈 정각부터 가능, 시작 정각부터 불가)
    public boolean isBookable(Schedule schedule, LocalDateTime now) {
        LocalDateTime startAt = schedule.getStartAt();
        return schedule.getStatus() == ScheduleStatus.OPEN
                && !now.isBefore(bookingOpenAt(startAt))
                && now.isBefore(startAt);
    }

    // 예매 오픈 시각: 회차 시작일 기준 7일 전 00:00
    private LocalDateTime bookingOpenAt(LocalDateTime startAt) {
        return startAt.toLocalDate().minusDays(BOOKING_OPEN_DAYS_BEFORE).atStartOfDay();
    }
}
