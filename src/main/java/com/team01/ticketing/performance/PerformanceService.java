package com.team01.ticketing.performance;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PerformanceService {

    private final PerformanceRepository performanceRepository;

    public PerformanceService(PerformanceRepository performanceRepository) {
        this.performanceRepository = performanceRepository;
    }

    public List<Performance> getPerformances() {
        return performanceRepository.findAllByOrderByIdAsc();
    }

    public Performance getPerformance(Long performanceId) {
        return performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND));
    }

    public Performance createPerformance(Performance performance) {
        if (performance.getStartDate() != null && performance.getEndDate() != null
                && performance.getEndDate().isBefore(performance.getStartDate())) {
            throw new BusinessException(ErrorCode.INVALID_PERFORMANCE_DATE_RANGE);
        }

        return performanceRepository.save(performance);
    }
}
