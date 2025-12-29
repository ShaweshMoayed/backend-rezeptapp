package com.example.rezeptapp.service;

import com.example.rezeptapp.model.Ingredient;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.model.UserAccount;
import com.example.rezeptapp.repository.RecipeRepository;
import com.example.rezeptapp.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository repo;
    private final UserAccountRepository userRepo;

    public RecipeService(RecipeRepository repo, UserAccountRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    // ✅ GET /rezeptapp?search=&category=   (favorite gehört NICHT mehr hier rein)
    public List<Recipe> findAll(String search, String category) {
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (!hasSearch && !hasCategory) return repo.findAll();

        if (hasCategory && hasSearch) {
            return repo.findByCategoryIgnoreCaseAndTitleContainingIgnoreCaseOrCategoryIgnoreCaseAndDescriptionContainingIgnoreCase(
                    category, search, category, search
            );
        }
        if (hasCategory) return repo.findByCategoryIgnoreCase(category);

        return repo.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
    }

    public Recipe findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe nicht gefunden: " + id));
    }

    @Transactional
    public Recipe create(Recipe recipe) {
        if (recipe.getIngredients() != null) {
            for (Ingredient ing : recipe.getIngredients()) {
                ing.setRecipe(recipe);
            }
        }
        return repo.save(recipe);
    }

    @Transactional
    public Recipe update(Long id, Recipe incoming) {
        Recipe existing = findById(id);

        existing.setTitle(incoming.getTitle());
        existing.setDescription(incoming.getDescription());
        existing.setInstructions(incoming.getInstructions());
        existing.setCategory(incoming.getCategory());
        existing.setImageUrl(incoming.getImageUrl());
        existing.setPrepMinutes(incoming.getPrepMinutes());
        existing.setServings(incoming.getServings());
        existing.setNutrition(incoming.getNutrition());
        existing.setIngredients(incoming.getIngredients());

        return repo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Recipe nicht gefunden: " + id);
        repo.deleteById(id);
    }

    // =========================
    // ✅ Favoriten pro User
    // =========================

    @Transactional(readOnly = true)
    public List<Recipe> getFavorites(UserAccount user) {
        return new ArrayList<>(user.getFavorites());
    }

    @Transactional(readOnly = true)
    public List<Long> getFavoriteIds(UserAccount user) {
        return user.getFavorites().stream().map(Recipe::getId).toList();
    }

    @Transactional
    public void addFavorite(UserAccount user, Long recipeId) {
        Recipe recipe = findById(recipeId);
        user.getFavorites().add(recipe);
        userRepo.save(user);
    }

    @Transactional
    public void removeFavorite(UserAccount user, Long recipeId) {
        Recipe recipe = findById(recipeId);
        user.getFavorites().remove(recipe);
        userRepo.save(user);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return repo.findAll().stream()
                .map(Recipe::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}