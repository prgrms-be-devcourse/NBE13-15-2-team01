package com.team01.ticketing.performance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "공연")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping({"/api/v1/performances", "/api/performances"})
    @Operation(summary = "공연 목록 조회")
    public List<Performance> getPerformances() {
        return performanceService.getPerformances();
    }

    @GetMapping("/api/v1/performances/{performanceId}")
    @Operation(summary = "공연 상세 조회")
    public Performance getPerformance(@PathVariable @Positive Long performanceId) {
        return performanceService.getPerformance(performanceId);
    }

    @PostMapping("/api/v1/performances")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "공연 등록")
    public Performance createPerformance(@Valid @RequestBody Performance performance) {
        return performanceService.createPerformance(performance);
    }

    @DeleteMapping("/api/v1/performances/{performanceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "회차가 없는 공연 삭제")
    public void deletePerformance(@PathVariable @Positive Long performanceId) {
        performanceService.deletePerformance(performanceId);
    }
}
