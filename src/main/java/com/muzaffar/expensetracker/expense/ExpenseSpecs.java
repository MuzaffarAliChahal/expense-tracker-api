package com.muzaffar.expensetracker.expense;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** Composable filters for the expense list. Only non-null filters are applied. */
final class ExpenseSpecs {

    private ExpenseSpecs() {
    }

    static Specification<Expense> ownedBy(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    static Specification<Expense> spentOnOrAfter(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("spentOn"), from);
    }

    static Specification<Expense> spentOnOrBefore(LocalDate to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("spentOn"), to);
    }

    static Specification<Expense> inCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    static Specification<Expense> filter(Long userId, LocalDate from, LocalDate to, Long categoryId) {
        Specification<Expense> spec = ownedBy(userId);
        if (from != null) spec = spec.and(spentOnOrAfter(from));
        if (to != null) spec = spec.and(spentOnOrBefore(to));
        if (categoryId != null) spec = spec.and(inCategory(categoryId));
        return spec;
    }
}
