package com.team01.ticketing.schedule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByPerformanceIdOrderByStartAtAscIdAsc(Long performanceId);
}
