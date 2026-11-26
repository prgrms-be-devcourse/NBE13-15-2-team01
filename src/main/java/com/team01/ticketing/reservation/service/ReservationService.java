package com.team01.ticketing.reservation.service;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;
import com.team01.ticketing.reservation.dto.ReservationCreateRequest;
import com.team01.ticketing.reservation.dto.ReservationResponse;
import com.team01.ticketing.reservation.entity.Reservation;
import com.team01.ticketing.reservation.repository.ReservationRepository;
import com.team01.ticketing.reservation.repository.ReservationSeatRepository;
import com.team01.ticketing.schedule.Schedule;
import com.team01.ticketing.schedule.ScheduleService;
import com.team01.ticketing.seat.Seat;
import com.team01.ticketing.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    // MySQL 에러 코드: UNIQUE 중복 (예매 저장 시 걸릴 수 있는 UNIQUE는 active_seat_id 하나)
    private static final int MYSQL_DUPLICATE_KEY = 1062;

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ScheduleService scheduleService;
    private final SeatRepository seatRepository;
    private final ReservationPolicy reservationPolicy;

    @Transactional
    public ReservationResponse reserve(Long memberId, ReservationCreateRequest request) {
        LocalDateTime now = reservationPolicy.now();
        Long scheduleId = request.scheduleId();
        List<Long> seatIds = request.seatIds();

        validateNoDuplicateSeats(seatIds);
        validateBookable(scheduleId, now);
        List<Seat> seats = findSeatsOfSchedule(scheduleId, seatIds);
        validateSeatsNotReserved(seatIds);

        Reservation reservation = saveReservation(memberId, scheduleId, seatIds, now);

        return ReservationResponse.of(reservation, seats);
    }

    // 같은 좌석 중복 선택 → DB 가기 전에 거름
    private void validateNoDuplicateSeats(List<Long> seatIds) {
        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    // 회차: 없으면 404(ScheduleService), 마감·시작 후면 409
    private void validateBookable(Long scheduleId, LocalDateTime now) {
        Schedule schedule = scheduleService.getSchedule(scheduleId);
        if (!reservationPolicy.isBookable(schedule, now)) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_BOOKABLE);
        }
    }

    // 이 회차 좌석 중 요청한 것만 조회 → 개수가 다르면 없는 좌석이나 다른 회차 좌석이 섞인 것
    private List<Seat> findSeatsOfSchedule(Long scheduleId, List<Long> seatIds) {
        List<Seat> seats = seatRepository.findAllByScheduleIdAndIdInOrderByIdAsc(scheduleId, seatIds);
        if (seats.size() != seatIds.size()) {
            throw new BusinessException(ErrorCode.SEAT_NOT_FOUND);
        }
        return seats;
    }

    // 이미 예매된 좌석: 먼저 확인 (대부분 여기서 걸러짐)
    private void validateSeatsNotReserved(List<Long> seatIds) {
        if (reservationSeatRepository.existsByActiveSeatIdIn(seatIds)) {
            throw new BusinessException(ErrorCode.SEAT_ALREADY_RESERVED);
        }
    }

    private Reservation saveReservation(Long memberId, Long scheduleId, List<Long> seatIds, LocalDateTime now) {

        Reservation reservation = Reservation.create(memberId, scheduleId, seatIds, now);

        try {
            return reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException e) {
            if (isSeatAlreadyReserved(e)) {
                throw new BusinessException(ErrorCode.SEAT_ALREADY_RESERVED);
            }
            throw e;
        }
    }

    // 예매 저장 시 걸릴 수 있는 UNIQUE는 active_seat_id 하나 → MySQL 중복 키(1062) = 이미 예매된 좌석
    private boolean isSeatAlreadyReserved(DataIntegrityViolationException e) {
        return e.getMostSpecificCause() instanceof SQLException sqlException
                && sqlException.getErrorCode() == MYSQL_DUPLICATE_KEY;
    }
}
