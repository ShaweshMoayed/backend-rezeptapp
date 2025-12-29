package com.example.rezeptapp.dto;

import java.time.Instant;
import java.util.List;

public record RecipeResponse(
        Long id,
        String title,
        String description,
        String instructions,
        String category,
        String imageUrl,
        Integer prepMinutes,
        Integer servings,
        boolean favorite,
        NutritionDto nutrition,
        List<IngredientDto> ingredients,
        Instant createdAt,
        Instant updatedAt
) {}