package com.team01.ticketing.schedule;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;
import com.team01.ticketing.performance.PerformanceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

    private final PerformanceRepository performanceRepository;
    private final ScheduleRepository scheduleRepository;

    public ScheduleService(PerformanceRepository performanceRepository, ScheduleRepository scheduleRepository) {
        this.performanceRepository = performanceRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedules(Long performanceId) {
        requirePerformance(performanceId);
        return scheduleRepository.findAllByPerformanceIdOrderByStartAtAscIdAsc(performanceId);
    }

    public Schedule getSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    @Transactional
    public Schedule createSchedule(Long performanceId, Schedule schedule) {
        requirePerformance(performanceId);
        schedule.setPerformanceId(performanceId);
        schedule.setStartAt(schedule.getStartAt().withNano(0));
        return scheduleRepository.save(schedule);
    }

    private void requirePerformance(Long performanceId) {
        if (!performanceRepository.existsById(performanceId)) {
            throw new BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND);
        }
    }
}
