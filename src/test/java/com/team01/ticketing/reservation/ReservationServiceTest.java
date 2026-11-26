package com.team01.ticketing.reservation;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;
import com.team01.ticketing.reservation.dto.ReservationCreateRequest;
import com.team01.ticketing.reservation.dto.ReservationResponse;
import com.team01.ticketing.reservation.entity.Reservation;
import com.team01.ticketing.reservation.entity.ReservationStatus;
import com.team01.ticketing.reservation.repository.ReservationRepository;
import com.team01.ticketing.reservation.repository.ReservationSeatRepository;
import com.team01.ticketing.reservation.service.ReservationPolicy;
import com.team01.ticketing.reservation.service.ReservationService;
import com.team01.ticketing.schedule.Schedule;
import com.team01.ticketing.schedule.ScheduleService;
import com.team01.ticketing.schedule.ScheduleStatus;
import com.team01.ticketing.seat.Seat;
import com.team01.ticketing.seat.SeatRepository;
import com.team01.ticketing.support.TestClocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservationServiceTest {

    // 기준 시각 12/1 12:00, 공연 12/8 19:00 → 예매 오픈(12/1 00:00) 지남 + 시작 전 = 예매 가능
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 12, 1, 12, 0);
    private static final LocalDateTime SHOW_START = LocalDateTime.of(2026, 12, 8, 19, 0);
    private static final Long MEMBER = 1L;
    private static final Long SCHEDULE = 10L;
    private static final int VIP_PRICE = 150000;
    private static final int R_PRICE = 110000;

    @Mock
    ReservationRepository reservationRepository;
    @Mock
    ReservationSeatRepository reservationSeatRepository;
    @Mock
    ScheduleService scheduleService;
    @Mock
    SeatRepository seatRepository;

    ReservationService reservationService;

    @BeforeEach
    void setUp() {
        // 정책은 가짜가 아니라 고정 시계를 넣은 진짜 객체 → 실제 시각 규칙 그대로 적용
        reservationService = new ReservationService(
                reservationRepository, reservationSeatRepository, scheduleService, seatRepository,
                new ReservationPolicy(TestClocks.fixedAt(NOW)));
    }

    @Test
    @DisplayName("열린 회차의 좌석을 예매하면 확정 상태로 저장되고 좌석과 총액을 돌려준다")
    void reserveSuccess() {
        givenSchedule(ScheduleStatus.OPEN);
        givenSeats(rSeat(2L), vipSeat(1L)); // 조회 결과를 일부러 역순으로 → 응답 정렬 확인
        given(reservationRepository.saveAndFlush(any(Reservation.class))).willAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = reserve(2L, 1L);

        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(response.seats()).extracting(ReservationResponse.SeatResponse::seatId).containsExactly(1L, 2L);
        assertThat(response.totalPrice()).isEqualTo(VIP_PRICE + R_PRICE);
        then(reservationRepository).should().saveAndFlush(any(Reservation.class));
    }

    @Test
    @DisplayName("같은 좌석을 중복으로 선택하면 회차 조회 전에 실패한다")
    void duplicateSeatIds() {
        BusinessException e = assertThrows(BusinessException.class, () -> reserve(1L, 1L));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        then(scheduleService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("예매할 수 없는 회차면 좌석 조회와 저장 없이 실패한다")
    void notBookableSchedule() {
        givenSchedule(ScheduleStatus.CLOSED);

        BusinessException e = assertThrows(BusinessException.class, () -> reserve(1L));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_BOOKABLE);
        then(seatRepository).shouldHaveNoInteractions();
        then(reservationRepository).should(never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("이 회차에 없는 좌석이 포함되면 예매할 수 없다")
    void seatNotFound() {
        givenSchedule(ScheduleStatus.OPEN);
        givenSeats(vipSeat(1L));

        BusinessException e = assertThrows(BusinessException.class, () -> reserve(1L, 99L));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SEAT_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 예매된 좌석이 있으면 저장 전에 실패한다")
    void seatAlreadyReservedBeforeSave() {
        givenBookableScheduleAndSeat();
        given(reservationSeatRepository.existsByActiveSeatIdIn(any())).willReturn(true);

        BusinessException e = assertThrows(BusinessException.class, () -> reserve(1L));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SEAT_ALREADY_RESERVED);
        then(reservationRepository).should(never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("동시 예매로 저장 중 중복 키(1062)가 나면 이미 예매된 좌석 예외로 바꿔 던진다")
    void seatAlreadyReservedOnSave() {
        givenBookableScheduleAndSeat();
        given(reservationRepository.saveAndFlush(any(Reservation.class))).willThrow(integrityViolation(1062));

        BusinessException e = assertThrows(BusinessException.class, () -> reserve(1L));

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SEAT_ALREADY_RESERVED);
    }

    @Test
    @DisplayName("중복 키가 아닌 제약 위반(1452)은 변환하지 않고 공통 예외 처리로 넘긴다")
    void otherIntegrityViolation() {
        givenBookableScheduleAndSeat();
        given(reservationRepository.saveAndFlush(any(Reservation.class))).willThrow(integrityViolation(1452));

        assertThrows(DataIntegrityViolationException.class, () -> reserve(1L));
    }

    private void givenBookableScheduleAndSeat() {
        givenSchedule(ScheduleStatus.OPEN);
        givenSeats(vipSeat(1L));
    }

    private void givenSchedule(ScheduleStatus status) {
        Schedule schedule = mock(Schedule.class);
        given(schedule.getStatus()).willReturn(status);
        given(schedule.getStartAt()).willReturn(SHOW_START);
        given(scheduleService.getSchedule(SCHEDULE)).willReturn(schedule);
    }

    private void givenSeats(Seat... seats) {
        given(seatRepository.findAllByScheduleIdAndIdInOrderByIdAsc(any(), any())).willReturn(List.of(seats));
    }

    private Seat vipSeat(Long id) {
        return seat(id, "VIP", VIP_PRICE);
    }

    private Seat rSeat(Long id) {
        return seat(id, "R", R_PRICE);
    }

    private Seat seat(Long id, String grade, int price) {
        Seat seat = mock(Seat.class);
        given(seat.getId()).willReturn(id);
        given(seat.getScheduleId()).willReturn(SCHEDULE);
        given(seat.getSeatName()).willReturn("A" + id);
        given(seat.getGrade()).willReturn(grade);
        given(seat.getPrice()).willReturn(price);
        return seat;
    }

    // 실제 MySQL처럼 원인(cause)에 SQL 예외와 에러 코드를 담음 (23000: 표준 무결성 위반, 뒤 숫자: MySQL 코드)
    private DataIntegrityViolationException integrityViolation(int mysqlErrorCode) {
        return new DataIntegrityViolationException("integrity violation",
                new SQLIntegrityConstraintViolationException("constraint fails", "23000", mysqlErrorCode));
    }

    private ReservationResponse reserve(Long... seatIds) {
        return reservationService.reserve(MEMBER, new ReservationCreateRequest(SCHEDULE, List.of(seatIds)));
    }
}
