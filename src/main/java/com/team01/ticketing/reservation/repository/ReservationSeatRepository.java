package com.team01.ticketing.reservation.repository;

import com.team01.ticketing.reservation.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    // 회차별 활성 예매 좌석 (좌석 현황 조회에서 사용)
    List<ReservationSeat> findByScheduleIdAndActiveSeatIdIsNotNull(Long scheduleId);

    // 회원이 해당 회차에 보유한 활성 좌석 수 (1인 N매 제한용)
    long countByMemberIdAndScheduleIdAndActiveSeatIdIsNotNull(Long memberId, Long scheduleId);

    // 요청한 좌석 중 이미 예매된(활성) 좌석이 있는지
    boolean existsByActiveSeatIdIn(Collection<Long> seatIds);
}
