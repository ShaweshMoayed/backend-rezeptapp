package com.example.rezeptapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RecipeCreateRequest(
        @NotBlank @Size(min = 2, max = 200) String title,
        @NotBlank @Size(min = 3, max = 2000) String description,
        String instructions,
        String category,
        String imageUrl,
        Integer prepMinutes,
        Integer servings,
        Boolean favorite,
        NutritionDto nutrition,
        @Valid List<IngredientDto> ingredients
) {}