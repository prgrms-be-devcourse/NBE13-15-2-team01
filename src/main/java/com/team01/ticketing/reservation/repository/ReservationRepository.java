package com.team01.ticketing.reservation.repository;

import com.team01.ticketing.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
