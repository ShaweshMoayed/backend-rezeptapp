package com.example.rezeptapp.controller;

import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rezeptapp")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    // ✅ GET /rezeptapp?search=...&category=...&favorite=true
    @GetMapping
    public List<Recipe> getAllRecipes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean favorite
    ) {
        return recipeService.findAll(search, category, favorite);
    }

    // ✅ GET /rezeptapp/{id}
    @GetMapping("/{id}")
    public Recipe getRecipeById(@PathVariable Long id) {
        return recipeService.findById(id);
    }

    // ✅ POST /rezeptapp
    @PostMapping
    public Recipe createRecipe(@Valid @RequestBody Recipe recipe) {
        return recipeService.create(recipe);
    }

    // ✅ PUT /rezeptapp/{id}
    @PutMapping("/{id}")
    public Recipe updateRecipe(@PathVariable Long id, @Valid @RequestBody Recipe recipe) {
        return recipeService.update(id, recipe);
    }

    // ✅ DELETE /rezeptapp/{id}
    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable Long id) {
        recipeService.delete(id);
    }
}