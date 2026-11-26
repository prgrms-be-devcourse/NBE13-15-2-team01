package com.team01.ticketing.kopis;

import java.time.LocalDate;

public record KopisPerformanceItem(
        String kopisId,
        String title,
        String genre,
        String posterUrl,
        String venueName,
        LocalDate startDate,
        LocalDate endDate) {
}