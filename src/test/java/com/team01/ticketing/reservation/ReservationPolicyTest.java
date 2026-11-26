package com.team01.ticketing.reservation;

import com.team01.ticketing.reservation.service.ReservationPolicy;
import com.team01.ticketing.schedule.Schedule;
import com.team01.ticketing.schedule.ScheduleStatus;
import com.team01.ticketing.support.TestClocks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ReservationPolicyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 12, 1, 12, 0);

    // 12/8 19:00 회차 → 예매 오픈은 12/1 00:00
    private static final LocalDateTime SHOW_START = LocalDateTime.of(2026, 12, 8, 19, 0);
    private final ReservationPolicy policy = new ReservationPolicy(TestClocks.fixedAt(NOW));

    @Test
    @DisplayName("now()는 주입한 Clock 기준 시각을 돌려준다")
    void nowUsesClock() {
        assertThat(policy.now()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("예매 오픈(시작 7일 전 00:00) 1초 전에는 예매할 수 없다")
    void notBookableOneSecondBeforeOpen() {
        LocalDateTime beforeOpen = LocalDateTime.of(2026, 11, 30, 23, 59, 59);
        assertThat(policy.isBookable(schedule(ScheduleStatus.OPEN, SHOW_START), beforeOpen)).isFalse();
    }

    @Test
    @DisplayName("예매 오픈 정각부터 예매할 수 있다")
    void bookableAtOpen() {
        LocalDateTime openAt = LocalDateTime.of(2026, 12, 1, 0, 0);
        assertThat(policy.isBookable(schedule(ScheduleStatus.OPEN, SHOW_START), openAt)).isTrue();
    }

    @Test
    @DisplayName("열린 회차는 시작 1초 전까지 예매할 수 있다")
    void bookableUntilOneSecondBeforeStart() {
        assertThat(policy.isBookable(schedule(ScheduleStatus.OPEN, SHOW_START), SHOW_START.minusSeconds(1))).isTrue();
    }

    @Test
    @DisplayName("시작 시각 정각부터는 예매할 수 없다")
    void notBookableAtStart() {
        assertThat(policy.isBookable(schedule(ScheduleStatus.OPEN, SHOW_START), SHOW_START)).isFalse();
    }

    @Test
    @DisplayName("마감된 회차는 예매 기간이어도 예매할 수 없다")
    void notBokkableAtStart() {
        assertThat(policy.isBookable(schedule(ScheduleStatus.CLOSED, SHOW_START), NOW)).isFalse();
    }

    private Schedule schedule(ScheduleStatus status, LocalDateTime startAt) {
        Schedule schedule = mock(Schedule.class);
        given(schedule.getStatus()).willReturn(status);
        given(schedule.getStartAt()).willReturn(startAt);
        return schedule;
    }
}
