package com.example.rezeptapp.service;

import com.example.rezeptapp.model.Ingredient;
import com.example.rezeptapp.model.Nutrition;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class StatsServiceTest {

    @Autowired StatsService statsService;
    @Autowired RecipeRepository recipeRepo;

    @BeforeEach
    void cleanDb() {
        recipeRepo.deleteAll();
    }

    @Test
    void buildStats_emptyIds_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> statsService.buildStats(new StatsService.StatsRequest(List.of())));
        assertTrue(ex.getMessage().toLowerCase().contains("darf nicht leer sein"));
    }

    @Test
    void buildStats_missingId_throwsWithList() {
        Recipe r1 = recipeRepo.save(recipe("R1", 100, 10, 5, 20));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> statsService.buildStats(new StatsService.StatsRequest(List.of(r1.getId(), 999999L))));
        assertTrue(ex.getMessage().toLowerCase().contains("nicht gefunden"));
        assertTrue(ex.getMessage().contains("999999"));
    }

    @Test
    void buildStats_duplicates_areIgnored_orderPreserved() {
        Recipe r1 = recipeRepo.save(recipe("R1", 100, 10, 5, 20));
        Recipe r2 = recipeRepo.save(recipe("R2", 200, 20, 10, 40));

        StatsService.StatsResponse res = statsService.buildStats(
                new StatsService.StatsRequest(List.of(r2.getId(), r1.getId(), r2.getId()))
        );

        // duplicates removed -> 2 recipes, order preserved (r2, r1)
        assertEquals(2, res.recipes().size());
        assertEquals(r2.getId(), res.recipes().get(0).id());
        assertEquals(r1.getId(), res.recipes().get(1).id());
    }

    @Test
    void buildStats_totals_areSummedCorrectly() {
        Recipe r1 = recipeRepo.save(recipe("R1", 100, 10, 5, 20));
        Recipe r2 = recipeRepo.save(recipe("R2", 200, 20, 10, 40));

        StatsService.StatsResponse res = statsService.buildStats(
                new StatsService.StatsRequest(List.of(r1.getId(), r2.getId()))
        );

        assertEquals(2, res.recipes().size());

        // totals
        assertEquals(300.0, res.total().caloriesKcal(), 0.0001);
        assertEquals(30.0, res.total().proteinG(), 0.0001);
        assertEquals(15.0, res.total().fatG(), 0.0001);
        assertEquals(60.0, res.total().carbsG(), 0.0001);

        // donut should match total (per implementation)
        assertEquals(res.total().proteinG(), res.donut().proteinG(), 0.0001);
        assertEquals(res.total().fatG(), res.donut().fatG(), 0.0001);
        assertEquals(res.total().carbsG(), res.donut().carbsG(), 0.0001);
    }

    // ===== helper =====
    private Recipe recipe(String title, int kcal, double p, double f, double c) {
        Recipe r = new Recipe();
        r.setTitle(title);
        r.setDescription("desc");
        r.setInstructions("step1");
        r.setCategory("Test");
        r.setPrepMinutes(10);
        r.setServings(1);

        Nutrition n = new Nutrition();
        n.setCaloriesKcal(kcal);
        n.setProteinG(p);
        n.setFatG(f);
        n.setCarbsG(c);
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