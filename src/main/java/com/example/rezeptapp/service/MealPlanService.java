package com.example.rezeptapp.service;

import com.example.rezeptapp.model.MealPlan;
import com.example.rezeptapp.model.MealPlanEntry;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.model.UserAccount;
import com.example.rezeptapp.repository.MealPlanRepository;
import com.example.rezeptapp.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MealPlanService {

    private final MealPlanRepository planRepo;
    private final RecipeRepository recipeRepo;

    public MealPlanService(MealPlanRepository planRepo, RecipeRepository recipeRepo) {
        this.planRepo = planRepo;
        this.recipeRepo = recipeRepo;
    }

    @Transactional(readOnly = true)
    public List<MealPlan> list(UserAccount user) {
        return planRepo.findByUserIdOrderByWeekStartMondayDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public MealPlan get(UserAccount user, Long planId) {
        return planRepo.findByIdAndUserId(planId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Plan nicht gefunden: " + planId));
    }

    @Transactional
    public void delete(UserAccount user, Long planId) {
        MealPlan existing = get(user, planId);
        planRepo.delete(existing);
    }

    @Transactional
    public MealPlan create(UserAccount user, String title, LocalDate weekStartMonday, List<MealPlanEntry> entries) {
        LocalDate monday = ensureMonday(weekStartMonday != null ? weekStartMonday : LocalDate.now());

        normalizeRecipes(entries);

        validateWeek(entries, monday);
        validateAtLeastOneRecipePerDay(entries, monday);

        MealPlan plan = new MealPlan();
        plan.setUser(user);
        plan.setTitle(title);
        plan.setWeekStartMonday(monday);
        plan.setEntries(entries);

        return planRepo.save(plan);
    }

    @Transactional
    public MealPlan update(UserAccount user, Long planId, String title, LocalDate weekStartMonday, List<MealPlanEntry> entries) {
        MealPlan existing = get(user, planId);

        if (title != null) existing.setTitle(title);

        LocalDate monday = existing.getWeekStartMonday();
        if (weekStartMonday != null) {
            monday = ensureMonday(weekStartMonday);
            existing.setWeekStartMonday(monday);
        }

        normalizeRecipes(entries);

        validateWeek(entries, monday);
        validateAtLeastOneRecipePerDay(entries, monday);

        existing.setEntries(entries);
        return planRepo.save(existing);
    }

    private void normalizeRecipes(List<MealPlanEntry> entries) {
        if (entries == null) return;

        for (MealPlanEntry e : entries) {
            if (e.getSlot() == null) {
                throw new IllegalArgumentException("slot darf nicht null sein");
            }
            if (e.getDay() == null) {
                throw new IllegalArgumentException("day darf nicht null sein");
            }

            // recipe optional: null = Slot leer
            if (e.getRecipe() != null && e.getRecipe().getId() != null) {
                Long rid = e.getRecipe().getId();
                Recipe r = recipeRepo.findById(rid)
                        .orElseThrow(() -> new IllegalArgumentException("Recipe nicht gefunden: " + rid));
                e.setRecipe(r);
            } else {
                e.setRecipe(null);
            }
        }
    }

    private void validateWeek(List<MealPlanEntry> entries, LocalDate monday) {
        List<MealPlanEntry> safe = entries == null ? List.of() : entries;
        LocalDate sunday = monday.plusDays(6);

        for (MealPlanEntry e : safe) {
            LocalDate d = e.getDay();
            if (d.isBefore(monday) || d.isAfter(sunday)) {
                throw new IllegalArgumentException("Plan-Eintrag liegt außerhalb der Woche (Mo–So).");
            }
        }
    }

    // ✅ pro Tag mindestens 1 Rezept (Slots optional)
    private void validateAtLeastOneRecipePerDay(List<MealPlanEntry> entries, LocalDate monday) {
        List<MealPlanEntry> safe = entries == null ? List.of() : entries;

        Set<LocalDate> daysWithRecipe = new HashSet<>();
        for (MealPlanEntry e : safe) {
            if (e.getRecipe() != null) {
                daysWithRecipe.add(e.getDay());
            }
        }

        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            if (!daysWithRecipe.contains(day)) {
                throw new IllegalArgumentException("Für jeden Tag muss mindestens ein Rezept ausgewählt werden (" + day + ").");
            }
        }
    }

    private LocalDate ensureMonday(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}