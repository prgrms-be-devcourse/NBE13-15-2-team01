package com.team01.ticketing.seat;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByScheduleIdOrderByIdAsc(Long scheduleId);

    // 회차에 속한 좌석 중 요청한 ID만 조회 (다른 회차 좌석은 DB에서 걸러짐)
    List<Seat> findAllByScheduleIdAndIdInOrderByIdAsc(Long scheduleId, Collection<Long> ids);
}
