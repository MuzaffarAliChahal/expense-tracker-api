package com.muzaffar.expensetracker.category;

import com.muzaffar.expensetracker.common.ConflictException;
import com.muzaffar.expensetracker.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    public record CategoryResponse(Long id, String name) {
        static CategoryResponse from(Category c) {
            return new CategoryResponse(c.getId(), c.getName());
        }
    }

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(Long userId) {
        return categories.findByUserIdOrderByNameAsc(userId).stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, String name) {
        String trimmed = name.trim();
        if (categories.existsByUserIdAndNameIgnoreCase(userId, trimmed)) {
            throw new ConflictException("Category '" + trimmed + "' already exists");
        }
        return CategoryResponse.from(categories.save(new Category(userId, trimmed)));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        categories.delete(getOwned(userId, id));
    }

    /** Loads a category only if it belongs to the user; other users' ids look like they do not exist. */
    @Transactional(readOnly = true)
    public Category getOwned(Long userId, Long id) {
        return categories.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Category " + id + " not found"));
    }
}
