package com.example.rezeptapp.repository;

import com.example.rezeptapp.model.MealPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    @EntityGraph(attributePaths = {"entries", "entries.recipe"})
    List<MealPlan> findByUserIdOrderByWeekStartMondayDesc(Long userId);

    @EntityGraph(attributePaths = {"entries", "entries.recipe"})
    Optional<MealPlan> findByIdAndUserId(Long id, Long userId);
}