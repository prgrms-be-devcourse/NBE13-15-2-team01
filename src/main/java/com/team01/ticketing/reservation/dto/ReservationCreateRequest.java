package com.team01.ticketing.reservation.dto;

import com.team01.ticketing.reservation.service.ReservationPolicy;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReservationCreateRequest(
        @NotNull(message = "회차를 선택해 주세요.")
        @Positive(message = "회차 ID가 올바르지 않습니다.")
        Long scheduleId,

        @NotEmpty(message = "좌석을 선택해 주세요.")
        @Size(max = ReservationPolicy.MAX_SEATS_PER_SCHEDULE, message = "한 번에 최대 {max}석까지 선택할 수 있습니다.")
        List<@NotNull @Positive Long> seatIds) {
}
