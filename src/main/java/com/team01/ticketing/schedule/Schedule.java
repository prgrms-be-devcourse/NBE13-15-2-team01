package com.team01.ticketing.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(name = "performance_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long performanceId;

    @Column(name = "start_at", nullable = false)
    @NotNull
    private LocalDateTime startAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private ScheduleStatus status;

    protected Schedule() {
    }

    public Long getId() {
        return id;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    void setPerformanceId(Long performanceId) {
        this.performanceId = performanceId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public ScheduleStatus getStatus() {
        return status;
    }
}
