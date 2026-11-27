package com.team01.ticketing.reservation;

import com.team01.ticketing.reservation.entity.Reservation;
import com.team01.ticketing.reservation.entity.ReservationSeat;
import com.team01.ticketing.reservation.repository.ReservationRepository;
import com.team01.ticketing.reservation.repository.ReservationSeatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Sql("/sql/reservation-fixture.sql")
class ReservationRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 12, 1, 12, 0);
    private static final Long MEMBER_1 = 1L, MEMBER_2 = 2L;
    private static final Long SCHEDULE = 1L;
    private static final Long SEAT_A1 = 1L, SEAT_A2 = 2L;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationSeatRepository reservationSeatRepository;

    @Test
    @DisplayName("같은 좌석을 두 번 예매하면 UNIQUE 제약으로 실패한다")
    void duplicateSeatReservationFails(){
        reservationRepository.saveAndFlush(Reservation.create(MEMBER_1, SCHEDULE, List.of(SEAT_A1), NOW));

        assertThatThrownBy(() ->
                reservationRepository.saveAndFlush(Reservation.create(MEMBER_2, SCHEDULE, List.of(SEAT_A1), NOW)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("예매를 취소하면 같은 좌석을 다시 예매할 수 있다")
    void canReserveAgainAfterCancel() {
        Reservation first = reservationRepository.saveAndFlush(
                Reservation.create(MEMBER_1, SCHEDULE, List.of(SEAT_A1, SEAT_A2), NOW));

        first.cancel(NOW);
        reservationRepository.flush();
        reservationRepository.saveAndFlush(Reservation.create(MEMBER_2, SCHEDULE, List.of(SEAT_A1), NOW));

        assertThat(reservedSeatIds()).containsExactly(SEAT_A1);
        assertThat(reservationSeatRepository.countByMemberIdAndScheduleIdAndActiveSeatIdIsNotNull(MEMBER_1, SCHEDULE))
                .isZero();
    }

    private List<Long> reservedSeatIds() {
        return reservationSeatRepository.findByScheduleIdAndActiveSeatIdIsNotNull(SCHEDULE).stream()
                .map(ReservationSeat::getSeatId)
                .toList();
    }
}
