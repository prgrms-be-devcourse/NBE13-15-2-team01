package com.team01.ticketing.seat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "seat")
class Seat {

    @Id
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "seat_name", nullable = false, length = 20)
    private String seatName;

    @Column(nullable = false, length = 20)
    private String grade;

    @Column(nullable = false)
    private int price;

    @Transient
    private boolean available;

    protected Seat() {
    }

    public Long getId() {
        return id;
    }

    public String getSeatName() {
        return seatName;
    }

    public String getGrade() {
        return grade;
    }

    public int getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return available;
    }

    void setAvailable(boolean available) {
        this.available = available;
    }
}
