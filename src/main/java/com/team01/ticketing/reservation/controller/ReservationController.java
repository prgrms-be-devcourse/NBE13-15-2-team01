package com.team01.ticketing.reservation.controller;

import com.team01.ticketing.reservation.dto.ReservationCreateRequest;
import com.team01.ticketing.reservation.dto.ReservationResponse;
import com.team01.ticketing.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Table(name = "예매")
@RestController
@RequestMapping("/api/v1/reservastions")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "예매 생성", description = "회차와 좌석(최대 3석)을 골라 예매합니다. 하나라도 예매할 수 없으면 전체 실패합니다.")
    public ReservationResponse reserve(@AuthenticationPrincipal Long memberId,
                                       @Valid @RequestBody ReservationCreateRequest request) {
        return reservationService.reserve(memberId, request);
    }
}
