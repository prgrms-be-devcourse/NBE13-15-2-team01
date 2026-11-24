package com.team01.ticketing.schedule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "공연 회차")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/api/v1/performances/{performanceId}/schedules")
    @Operation(summary = "공연별 회차 목록 조회")
    public List<Schedule> getSchedules(@PathVariable @Positive Long performanceId) {
        return scheduleService.getSchedules(performanceId);
    }

    @PostMapping("/api/v1/performances/{performanceId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "공연 회차 등록")
    public Schedule createSchedule(@PathVariable @Positive Long performanceId,
                                   @Valid @RequestBody Schedule schedule) {
        return scheduleService.createSchedule(performanceId, schedule);
    }
}
