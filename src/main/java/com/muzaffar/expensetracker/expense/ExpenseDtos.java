package com.muzaffar.expensetracker.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ExpenseDtos {

    private ExpenseDtos() {
    }

    public record ExpenseRequest(
            @Schema(example = "42.50") @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
            @Schema(example = "Lunch with client") @Size(max = 255) String description,
            @Schema(example = "2026-10-01") @NotNull @PastOrPresent LocalDate spentOn,
            @Schema(example = "1") Long categoryId) {
    }

    public record ExpenseResponse(Long id, BigDecimal amount, String description, LocalDate spentOn,
                                  Long categoryId, String categoryName) {

        static ExpenseResponse from(Expense e) {
            var c = e.getCategory();
            return new ExpenseResponse(e.getId(), e.getAmount(), e.getDescription(), e.getSpentOn(),
                    c == null ? null : c.getId(), c == null ? null : c.getName());
        }
    }
}
