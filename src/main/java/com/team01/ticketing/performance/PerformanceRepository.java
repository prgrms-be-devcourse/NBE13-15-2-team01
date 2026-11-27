package com.team01.ticketing.performance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    List<Performance> findAllByOrderByIdAsc();
    Optional<Performance> findByKopisId(String kopisId);

    @Modifying
    @Query("delete from Performance p where p.id = :performanceId")
    int deletePerformance(@Param("performanceId") Long performanceId);
}
