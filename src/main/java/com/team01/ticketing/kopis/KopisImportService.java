package com.team01.ticketing.kopis;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;
import com.team01.ticketing.performance.Performance;
import com.team01.ticketing.performance.PerformanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class KopisImportService {

    private static final int WINDOW_DAYS = 30;
    private static final int MAX_PAGES_PER_WINDOW = 50;
    private static final long MAX_RANGE_DAYS = 366;

    private final KopisClient kopisClient;
    private final PerformanceRepository performanceRepository;
    private final TransactionTemplate transactionTemplate;

    public KopisImportService(
            KopisClient kopisClient,
            PerformanceRepository performanceRepository,
            PlatformTransactionManager transactionManager) {
        this.kopisClient = kopisClient;
        this.performanceRepository = performanceRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public KopisImportResult importRange(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BusinessException(ErrorCode.INVALID_PERFORMANCE_DATE_RANGE);
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Map<String, KopisPerformanceItem> collected = new LinkedHashMap<>();
        for (LocalDate start = from; !start.isAfter(to); start = start.plusDays(WINDOW_DAYS)) {
            LocalDate end = start.plusDays(WINDOW_DAYS - 1L);
            if (end.isAfter(to)) {
                end = to;
            }
            for (int page = 1; page <= MAX_PAGES_PER_WINDOW; page++) {
                List<KopisPerformanceItem> items = kopisClient.fetchPerformances(start, end, page);
                items.forEach(item -> collected.putIfAbsent(item.kopisId(), item));
                if (items.size() < KopisClient.PAGE_SIZE) {
                    break;
                }
            }
        }

        return transactionTemplate.execute(status -> persist(collected.values()));
    }

    private KopisImportResult persist(Collection<KopisPerformanceItem> items) {
        int created = 0;
        int updated = 0;
        for (KopisPerformanceItem item : items) {
            String kopisId = cut(item.kopisId(), 50);
            String title = cut(item.title(), 200);
            String genre = cut(item.genre(), 50);
            String posterUrl = cut(item.posterUrl(), 500);
            String venueName = cut(item.venueName(), 200);

            Optional<Performance> existing = performanceRepository.findByKopisId(kopisId);
            if (existing.isPresent()) {
                existing.get().updateFromKopis(
                        title, genre, posterUrl, venueName, item.startDate(), item.endDate());
                updated++;
            } else {
                performanceRepository.save(Performance.ofKopis(
                        kopisId, title, genre, posterUrl, venueName, item.startDate(), item.endDate()));
                created++;
            }
        }
        return new KopisImportResult(items.size(), created, updated);
    }

    private String cut(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    public record KopisImportResult(int fetched, int created, int updated) {
    }
}