package com.muzaffar.expensetracker.report;

import java.math.BigDecimal;

public record CategoryTotal(String category, BigDecimal total, Long count) {
}
