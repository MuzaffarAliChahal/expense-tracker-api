package com.muzaffar.expensetracker.report;

import com.muzaffar.expensetracker.expense.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
public class ReportService {

    private final ExpenseRepository expenses;

    public ReportService(ExpenseRepository expenses) {
        this.expenses = expenses;
    }

    @Transactional(readOnly = true)
    public MonthlyReport monthly(Long userId, YearMonth month) {
        List<CategoryTotal> totals = expenses.totalsByCategory(userId, month.atDay(1), month.atEndOfMonth());
        BigDecimal total = totals.stream().map(CategoryTotal::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        long count = totals.stream().mapToLong(CategoryTotal::count).sum();
        return new MonthlyReport(month.getYear(), month.getMonthValue(), total, count, totals);
    }
}
