package com.team01.ticketing.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.PERSIST)
    private List<ReservationSeat> seats = new ArrayList<>();

    public static Reservation create(Long memberId, Long scheduleId, List<Long> seatIds, LocalDateTime now) {
        Reservation reservation = new Reservation();
        reservation.memberId = memberId;
        reservation.scheduleId = scheduleId;
        reservation.status = ReservationStatus.CONFIRMED; // 결제 없는 MVP는 바로 확정
        reservation.reservedAt = now;

        // 좌석 ID 오름차순으로 저장 → 여러 좌석 동시 예매 시 데드락 위험 줄임
        seatIds.stream()
                .sorted()
                .forEach(seatId -> reservation.seats.add(ReservationSeat.create(reservation, seatId)));
        return reservation;
    }

    public void cancel(LocalDateTime now) {
        if (status == ReservationStatus.CANCELED) {
            throw new BusinessException(ErrorCode.RESERVATION_ALREADY_CANCELED);
        }
        status = ReservationStatus.CANCELED;
        canceledAt = now;
        seats.forEach(seat -> seat.cancel(now));
    }
}
