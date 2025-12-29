package com.example.rezeptapp.service;

import com.example.rezeptapp.model.Ingredient;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository repo;

    public RecipeService(RecipeRepository repo) {
        this.repo = repo;
    }

    // ✅ GET /rezeptapp?search=...&category=...&favorite=true
    public List<Recipe> findAll(String search, String category, Boolean favorite) {
        // "blank" zu null/"" normalisieren, damit Query sauber funktioniert
        if (search != null && search.isBlank()) search = "";
        if (category != null && category.isBlank()) category = "";

        return repo.search(search, category, favorite);
    }

    public Recipe findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe nicht gefunden: " + id));
    }

    @Transactional
    public Recipe create(Recipe recipe) {
        // Zutaten korrekt verknüpfen (recipe_id setzen)
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
        existing.setFavorite(incoming.isFavorite());
        existing.setNutrition(incoming.getNutrition());

        // Zutaten komplett ersetzen (inkl. Back-Reference)
        existing.setIngredients(incoming.getIngredients());

        return repo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Recipe nicht gefunden: " + id);
        }
        repo.deleteById(id);
    }
}