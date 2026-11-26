package com.team01.ticketing.kopis;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/kopis")
@Tag(name = "관리자 KOPIS")
public class KopisImportController {

    private final KopisImportService kopisImportService;

    public KopisImportController(KopisImportService kopisImportService) {
        this.kopisImportService = kopisImportService;
    }

    @PostMapping("/import")
    @Operation(summary = "KOPIS 공연 가져오기")
    public KopisImportService.KopisImportResult importPerformances(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return kopisImportService.importRange(from, to);
    }
}