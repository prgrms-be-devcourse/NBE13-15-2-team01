package com.team01.ticketing.performance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    List<Performance> findAllByOrderByIdAsc();
    Optional<Performance> findByKopisId(String kopisId);
}
