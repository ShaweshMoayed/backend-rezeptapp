package com.example.rezeptapp.service;

import com.example.rezeptapp.model.Ingredient;
import com.example.rezeptapp.model.Nutrition;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.model.UserAccount;
import com.example.rezeptapp.repository.RecipeRepository;
import com.example.rezeptapp.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RecipeServiceTest {

    @Autowired RecipeService recipeService;
    @Autowired RecipeRepository recipeRepo;
    @Autowired UserAccountRepository userRepo;

    private Recipe validRecipe(String title) {
        Recipe r = new Recipe();
        r.setTitle(title);
        r.setDescription("desc ok");
        r.setInstructions("step 1");
        r.setCategory("Test");

        Nutrition n = new Nutrition();
        n.setCaloriesKcal(100);
        n.setProteinG(10.0);
        n.setFatG(5.0);
        n.setCarbsG(20.0);
        r.setNutrition(n);

        Ingredient i = new Ingredient();
        i.setName("Zutat");
        i.setAmount("1");
        i.setUnit("Stk");
        r.setIngredients(List.of(i));

        return r;
    }

    @Test
    @Transactional
    void updateForUser_owner_canUpdateTitle() {
        // Arrange: valides Rezept speichern
        Recipe created = recipeService.createForUser(validRecipe("Alt"), "alice");

        Recipe patch = new Recipe();
        patch.setTitle("Neu");

        // Act
        Recipe updated = recipeService.updateForUser(created.getId(), patch, "alice");

        // Assert
        assertEquals("Neu", updated.getTitle());
        assertEquals("desc ok", updated.getDescription()); // unverändert
    }

    @Test
    @Transactional
    void favorites_addAndRemove_works() {
        // Arrange
        Recipe created = recipeService.createForUser(validRecipe("Fav"), "alice");

        UserAccount u = new UserAccount("alice", "hash");
        userRepo.save(u);

        // Act
        recipeService.addFavorite(u, created.getId());
        assertTrue(recipeService.getFavoriteIds(u).contains(created.getId()));

        recipeService.removeFavorite(u, created.getId());
        assertFalse(recipeService.getFavoriteIds(u).contains(created.getId()));
    }
}