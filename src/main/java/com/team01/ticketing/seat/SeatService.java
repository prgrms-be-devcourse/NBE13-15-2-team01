package com.team01.ticketing.seat;

import com.team01.ticketing.reservation.entity.ReservationSeat;
import com.team01.ticketing.reservation.repository.ReservationSeatRepository;
import com.team01.ticketing.schedule.Schedule;
import com.team01.ticketing.schedule.ScheduleService;
import com.team01.ticketing.schedule.ScheduleStatus;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SeatService {

    private final ScheduleService scheduleService;
    private final SeatRepository seatRepository;
    private final ReservationSeatRepository reservationSeatRepository;

    SeatService(ScheduleService scheduleService, SeatRepository seatRepository,
                ReservationSeatRepository reservationSeatRepository) {
        this.scheduleService = scheduleService;
        this.seatRepository = seatRepository;
        this.reservationSeatRepository = reservationSeatRepository;
    }

    @Transactional(readOnly = true)
    public List<Seat> getSeats(Long scheduleId) {
        Schedule schedule = scheduleService.getSchedule(scheduleId);
        List<Seat> seats = seatRepository.findAllByScheduleIdOrderByIdAsc(scheduleId);
        Set<Long> reservedSeatIds = reservationSeatRepository.findByScheduleIdAndActiveSeatIdIsNotNull(scheduleId)
                .stream()
                .map(ReservationSeat::getActiveSeatId)
                .collect(Collectors.toSet());

        for (Seat seat : seats) {
            boolean reserved = reservedSeatIds.contains(seat.getId());
            seat.setAvailable(schedule.getStatus() == ScheduleStatus.OPEN && !reserved);
        }
        return seats;
    }
}
