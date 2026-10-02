package com.muzaffar.expensetracker.expense;

import com.muzaffar.expensetracker.report.CategoryTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    @Query("""
            select new com.muzaffar.expensetracker.report.CategoryTotal(
                       coalesce(c.name, 'Uncategorized'), sum(e.amount), count(e))
            from Expense e left join e.category c
            where e.userId = :userId and e.spentOn between :start and :end
            group by c.name
            order by sum(e.amount) desc
            """)
    List<CategoryTotal> totalsByCategory(@Param("userId") Long userId,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);
}
