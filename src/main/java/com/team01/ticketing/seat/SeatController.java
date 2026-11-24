package com.team01.ticketing.seat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "좌석")
public class SeatController {

    private final SeatService seatService;

    SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/api/v1/schedules/{scheduleId}/seats")
    @Operation(summary = "회차별 좌석 현황 조회")
    public List<Seat> getSeats(@PathVariable @Positive Long scheduleId) {
        return seatService.getSeats(scheduleId);
    }
}
