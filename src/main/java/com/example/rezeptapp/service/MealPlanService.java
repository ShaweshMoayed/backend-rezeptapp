package com.example.rezeptapp.service;

import com.example.rezeptapp.model.MealPlan;
import com.example.rezeptapp.model.MealPlanEntry;
import com.example.rezeptapp.model.MealSlot;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.model.UserAccount;
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

    private final RecipeRepository recipeRepo;

    public MealPlanService(RecipeRepository recipeRepo) {
        this.recipeRepo = recipeRepo;
    }


    @Transactional(readOnly = true)
    public MealPlan buildTransientPlan(
            UserAccount user,
            String title,
            LocalDate weekStartMonday,
            List<MealPlanEntry> entries
    ) {
        if (user == null) throw new IllegalArgumentException("unauthorized");

        // entries darf nicht leer sein
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("entries darf nicht leer sein");
        }

        LocalDate monday = ensureMonday(weekStartMonday != null ? weekStartMonday : LocalDate.now());

        // keine Vergangenheit erlauben (ab aktueller Woche)
        LocalDate minMonday = ensureMonday(LocalDate.now());
        if (monday.isBefore(minMonday)) {
            throw new IllegalArgumentException("Du kannst keinen Wochenplan in der Vergangenheit erstellen.");
        }

        normalizeRecipes(entries);
        validateWeek(entries, monday);
        validateAtLeastOneRecipePerDay(entries, monday);

        MealPlan plan = new MealPlan();
        plan.setTitle((title == null || title.isBlank()) ? "Wochenplan" : title.trim());
        plan.setWeekStartMonday(monday);
        plan.setEntries(entries);

        return plan;
    }

    private void normalizeRecipes(List<MealPlanEntry> entries) {
        for (MealPlanEntry e : entries) {
            if (e.getSlot() == null) throw new IllegalArgumentException("slot darf nicht null sein");
            if (e.getDay() == null) throw new IllegalArgumentException("day darf nicht null sein");

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
        LocalDate sunday = monday.plusDays(6);

        for (MealPlanEntry e : entries) {
            LocalDate d = e.getDay();
            if (d.isBefore(monday) || d.isAfter(sunday)) {
                throw new IllegalArgumentException("Plan-Eintrag liegt außerhalb der Woche (Mo–So).");
            }
        }
    }

    // pro Tag mindestens 1 Rezept (Slots optional)
    private void validateAtLeastOneRecipePerDay(List<MealPlanEntry> entries, LocalDate monday) {
        Set<LocalDate> daysWithRecipe = new HashSet<>();
        for (MealPlanEntry e : entries) {
            if (e.getRecipe() != null) {
                daysWithRecipe.add(e.getDay());
            }
        }

        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            if (!daysWithRecipe.contains(day)) {
                throw new IllegalArgumentException(
                        "Bitte wähle für jeden Tag mindestens ein Rezept aus (" + day + ")."
                );
            }
        }
    }

    private LocalDate ensureMonday(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}