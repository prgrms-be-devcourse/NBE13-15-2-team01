package com.team01.ticketing.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_seat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    // 활성 상태면 seat_id와 같은 값, 취소되면 NULL → schema.sql의 UNIQUE로 중복 예매 차단
    @Column(name = "active_seat_id")
    private Long activeSeatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    static ReservationSeat create(Reservation reservation, Long seatId) {
        ReservationSeat seat = new ReservationSeat();
        seat.reservation = reservation;
        seat.memberId = reservation.getMemberId();
        seat.scheduleId = reservation.getScheduleId();
        seat.seatId = seatId;
        seat.activeSeatId = seatId;
        seat.status = reservation.getStatus();
        return seat;
    }

    // Reservation.cancel()에서만 호출
    void cancel(LocalDateTime now) {
        status = ReservationStatus.CANCELED;
        activeSeatId = null;
        canceledAt = now;
    }
}
