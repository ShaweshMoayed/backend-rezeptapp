package com.example.rezeptapp.service;

import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RezeptService {

    private final RecipeRepository recipeRepository;

    public RezeptService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }

    public Recipe create(Recipe rezept) {
        return recipeRepository.save(rezept);
    }
}