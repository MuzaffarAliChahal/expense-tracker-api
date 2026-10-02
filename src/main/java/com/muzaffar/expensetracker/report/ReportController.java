package com.muzaffar.expensetracker.report;

import com.muzaffar.expensetracker.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports")
@Validated
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/monthly")
    @Operation(summary = "Total spend for one month, broken down by category")
    public MonthlyReport monthly(@AuthenticationPrincipal AuthUser user,
                                 @RequestParam @Min(2000) @Max(2100) int year,
                                 @RequestParam @Min(1) @Max(12) int month) {
        return service.monthly(user.id(), YearMonth.of(year, month));
    }
}
