package com.muzaffar.expensetracker.expense;

import com.muzaffar.expensetracker.category.Category;
import com.muzaffar.expensetracker.category.CategoryService;
import com.muzaffar.expensetracker.common.NotFoundException;
import com.muzaffar.expensetracker.common.PageResponse;
import com.muzaffar.expensetracker.expense.ExpenseDtos.ExpenseRequest;
import com.muzaffar.expensetracker.expense.ExpenseDtos.ExpenseResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ExpenseService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("spentOn"), Sort.Order.desc("id"));

    private final ExpenseRepository expenses;
    private final CategoryService categoryService;

    public ExpenseService(ExpenseRepository expenses, CategoryService categoryService) {
        this.expenses = expenses;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> list(Long userId, LocalDate from, LocalDate to, Long categoryId,
                                              int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), NEWEST_FIRST);
        var result = expenses.findAll(ExpenseSpecs.filter(userId, from, to, categoryId), pageable)
                .map(ExpenseResponse::from);
        return PageResponse.of(result);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long userId, Long id) {
        return ExpenseResponse.from(getOwned(userId, id));
    }

    @Transactional
    public ExpenseResponse create(Long userId, ExpenseRequest request) {
        Expense expense = new Expense(userId);
        apply(userId, expense, request);
        return ExpenseResponse.from(expenses.save(expense));
    }

    @Transactional
    public ExpenseResponse update(Long userId, Long id, ExpenseRequest request) {
        Expense expense = getOwned(userId, id);
        apply(userId, expense, request);
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        expenses.delete(getOwned(userId, id));
    }

    private void apply(Long userId, Expense expense, ExpenseRequest r) {
        Category category = r.categoryId() == null ? null : categoryService.getOwned(userId, r.categoryId());
        String description = r.description() == null ? null : r.description().trim();
        expense.update(r.amount(), description, r.spentOn(), category);
    }

    private Expense getOwned(Long userId, Long id) {
        return expenses.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Expense " + id + " not found"));
    }
}
