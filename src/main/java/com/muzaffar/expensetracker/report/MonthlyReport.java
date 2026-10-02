package com.muzaffar.expensetracker.report;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyReport(int year, int month, BigDecimal total, long count, List<CategoryTotal> byCategory) {
}
