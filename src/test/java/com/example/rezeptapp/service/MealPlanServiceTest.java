package com.example.rezeptapp.service;

import com.example.rezeptapp.model.*;
import com.example.rezeptapp.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MealPlanServiceTest {

    @Autowired MealPlanService mealPlanService;
    @Autowired RecipeRepository recipeRepo;

    @BeforeEach
    void cleanDb() {
        recipeRepo.deleteAll();
    }

    @Test
    void buildTransientPlan_entriesEmpty_throws() {
        UserAccount user = new UserAccount("u", "hash");
        LocalDate monday = currentMonday();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mealPlanService.buildTransientPlan(user, "t", monday, List.of()));
        assertEquals("entries darf nicht leer sein", ex.getMessage());
    }

    @Test
    void buildTransientPlan_pastWeek_throws() {
        UserAccount user = new UserAccount("u", "hash");
        LocalDate pastMonday = currentMonday().minusWeeks(1);

        Recipe saved = recipeRepo.save(buildMinimalRecipe("R1"));
        List<MealPlanEntry> entries = oneRecipePerDayEntries(pastMonday, saved.getId());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mealPlanService.buildTransientPlan(user, "t", pastMonday, entries));
        assertTrue(ex.getMessage().toLowerCase().contains("vergangenheit"));
    }

    @Test
    void buildTransientPlan_invalidRecipeId_throws() {
        UserAccount user = new UserAccount("u", "hash");
        LocalDate monday = currentMonday();

        List<MealPlanEntry> entries = oneRecipePerDayEntries(monday, 999999L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mealPlanService.buildTransientPlan(user, "t", monday, entries));
        assertTrue(ex.getMessage().toLowerCase().contains("recipe nicht gefunden"));
    }

    @Test
    void buildTransientPlan_entryOutsideWeek_throws() {
        UserAccount user = new UserAccount("u", "hash");
        LocalDate monday = currentMonday();

        Recipe saved = recipeRepo.save(buildMinimalRecipe("R1"));

        MealPlanEntry e = new MealPlanEntry();
        e.setDay(monday.minusDays(1)); // outside Mo–So
        e.setSlot(MealSlot.BREAKFAST);
        Recipe r = new Recipe();
        r.setId(saved.getId());
        e.setRecipe(r);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mealPlanService.buildTransientPlan(user, "t", monday, List.of(e)));
        assertTrue(ex.getMessage().toLowerCase().contains("außerhalb der woche"));
    }

    @Test
    void buildTransientPlan_requiresAtLeastOneRecipePerDay_throws() {
        UserAccount user = new UserAccount("u", "hash");
        LocalDate monday = currentMonday();

        Recipe saved = recipeRepo.save(buildMinimalRecipe("R1"));

        // only 6 days filled -> should fail
        List<MealPlanEntry> entries = oneRecipePerDayEntries(monday, saved.getId()).subList(0, 6);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mealPlanService.buildTransientPlan(user, "t", monday, entries));
        assertTrue(ex.getMessage().toLowerCase().contains("mindestens ein rezept"));
    }

    @Test
    void buildTransientPlan_valid_returnsPlanWithResolvedRecipes() {
        UserAccount user = new UserAccount("u", "hash");
        LocalDate monday = currentMonday();

        Recipe saved = recipeRepo.save(buildMinimalRecipe("R1"));
        List<MealPlanEntry> entries = oneRecipePerDayEntries(monday, saved.getId());

        MealPlan plan = mealPlanService.buildTransientPlan(user, "  Mein Plan  ", monday.plusDays(2), entries);

        // weekStart is normalized to Monday
        assertEquals(monday, plan.getWeekStartMonday());
        assertEquals("Mein Plan", plan.getTitle());
        assertEquals(7, plan.getEntries().size());

        // recipes should be resolved (entity loaded from repo)
        assertNotNull(plan.getEntries().get(0).getRecipe());
        assertNotNull(plan.getEntries().get(0).getRecipe().getTitle());
    }

    // ===== helpers =====

    private LocalDate currentMonday() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private List<MealPlanEntry> oneRecipePerDayEntries(LocalDate monday, Long recipeId) {
        return List.of(
                entry(monday.plusDays(0), MealSlot.BREAKFAST, recipeId),
                entry(monday.plusDays(1), MealSlot.BREAKFAST, recipeId),
                entry(monday.plusDays(2), MealSlot.BREAKFAST, recipeId),
                entry(monday.plusDays(3), MealSlot.BREAKFAST, recipeId),
                entry(monday.plusDays(4), MealSlot.BREAKFAST, recipeId),
                entry(monday.plusDays(5), MealSlot.BREAKFAST, recipeId),
                entry(monday.plusDays(6), MealSlot.BREAKFAST, recipeId)
        );
    }

    private MealPlanEntry entry(LocalDate day, MealSlot slot, Long recipeId) {
        MealPlanEntry e = new MealPlanEntry();
        e.setDay(day);
        e.setSlot(slot);
        if (recipeId != null) {
            Recipe r = new Recipe();
            r.setId(recipeId);
            e.setRecipe(r);
        } else {
            e.setRecipe(null);
        }
        e.setServings(null);
        return e;
    }

    private Recipe buildMinimalRecipe(String title) {
        Recipe r = new Recipe();
        r.setTitle(title);
        r.setDescription("desc");
        r.setInstructions("step1");
        r.setCategory("Test");
        r.setPrepMinutes(10);
        r.setServings(2);

        Nutrition n = new Nutrition();
        n.setCaloriesKcal(100);
        n.setProteinG(10.0);
        n.setFatG(5.0);
        n.setCarbsG(20.0);
        r.setNutrition(n);

        Ingredient ing = new Ingredient();
        ing.setName("Salt");
        ing.setAmount("1");
        ing.setUnit("tsp");
        ing.setRecipe(r);
        r.setIngredients(List.of(ing));

        return r;
    }
}