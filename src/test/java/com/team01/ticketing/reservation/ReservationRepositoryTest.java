package com.team01.ticketing.reservation;

import com.team01.ticketing.reservation.entity.Reservation;
import com.team01.ticketing.reservation.entity.ReservationSeat;
import com.team01.ticketing.reservation.repository.ReservationRepository;
import com.team01.ticketing.reservation.repository.ReservationSeatRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ReservationRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationSeatRepository reservationSeatRepository;

    @Autowired
    EntityManager em;

    static final Long MEMBER_1 = 1L, MEMBER_2 = 2L;
    static final Long SCHEDULE = 1L;
    static final Long SEAT_A1 = 1L, SEAT_A2 = 2L;

    LocalDateTime now = LocalDateTime.of(2026, 11, 24, 12, 0);

    // 회원 2, 공연 1, 회차 1, 좌석 2 (FK 때문에 먼저 필요, 테스트 끝나면 롤백)
    @BeforeEach
    void setUp() {
        sql("INSERT INTO member (id, email, password, name, role) VALUES "
                + "(1, 'user1@test.com', 'pw', '회원1', 'USER'), (2, 'user2@test.com', 'pw', '회원2', 'USER')");
        sql("INSERT INTO performance (id, title) VALUES (1, '테스트 공연')");
        sql("INSERT INTO schedule (id, performance_id, start_at, status) VALUES (1, 1, '2026-12-31 19:00:00', 'OPEN')");
        sql("INSERT INTO seat (id, schedule_id, seat_name, grade, price) VALUES "
                + "(1, 1, 'A1', 'VIP', 150000), (2, 1, 'A2', 'VIP', 150000)");
    }

    @Test
    @DisplayName("같은 좌석을 두 번 예매하면 UNIQUE 제약으로 실패한다")
    void duplicateSeatReservationFails(){
        reservationRepository.saveAndFlush(Reservation.create(MEMBER_1, SCHEDULE, List.of(SEAT_A1), now));

        assertThatThrownBy(() ->
                reservationRepository.saveAndFlush(Reservation.create(MEMBER_2, SCHEDULE, List.of(SEAT_A1), now)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("예매를 취소하면 같은 좌석을 다시 예매할 수 있다")
    void canReserveAgainAfterCancel() {
        Reservation first = reservationRepository.saveAndFlush(
                Reservation.create(MEMBER_1, SCHEDULE, List.of(SEAT_A1, SEAT_A2), now));

        first.cancel(now);
        reservationRepository.flush();
        reservationRepository.saveAndFlush(Reservation.create(MEMBER_2, SCHEDULE, List.of(SEAT_A1), now));

        assertThat(reservedSeatIds()).containsExactly(SEAT_A1);
        assertThat(reservationSeatRepository.countByMemberIdAndScheduleIdAndActiveSeatIdIsNotNull(MEMBER_1, SCHEDULE))
                .isZero();
    }

    private void sql(String query) {
        em.createNativeQuery(query).executeUpdate();
    }

    private List<Long> reservedSeatIds() {
        return reservationSeatRepository.findByScheduleIdAndActiveSeatIdIsNotNull(SCHEDULE).stream()
                .map(ReservationSeat::getSeatId)
                .toList();
    }
}
