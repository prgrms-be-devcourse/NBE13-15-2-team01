package com.team01.ticketing.reservation.dto;

import com.team01.ticketing.reservation.entity.Reservation;
import com.team01.ticketing.reservation.entity.ReservationStatus;
import com.team01.ticketing.seat.Seat;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record ReservationResponse(
        Long reservationId,
        Long scheduleId,
        ReservationStatus status,
        LocalDateTime reservedAt,
        int totalPrice,
        List<SeatResponse> seats) {

    public record SeatResponse(Long seatId, String seatName, String grade, int price) {
        public static SeatResponse from(Seat seat){
            return new SeatResponse(seat.getId(), seat.getSeatName(), seat.getGrade(), seat.getPrice());
        }
    }

    public static ReservationResponse of(Reservation reservation, List<Seat> seats) {
        List<SeatResponse> seatResponses = seats.stream()
                .sorted(Comparator.comparing(Seat::getId))
                .map(SeatResponse::from)
                .toList();
        int totalPrice = seatResponses.stream()
                .mapToInt(SeatResponse::price)
                .sum();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getScheduleId(),
                reservation.getStatus(),
                reservation.getReservedAt(),
                totalPrice,
                seatResponses
        );
    }
}
