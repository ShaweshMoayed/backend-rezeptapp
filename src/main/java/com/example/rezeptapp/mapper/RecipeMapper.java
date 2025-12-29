package com.example.rezeptapp.mapper;

import com.example.rezeptapp.dto.*;
import com.example.rezeptapp.model.Ingredient;
import com.example.rezeptapp.model.Nutrition;
import com.example.rezeptapp.model.Recipe;

import java.util.List;

public class RecipeMapper {

    public static RecipeResponse toResponse(Recipe r) {
        return new RecipeResponse(
                r.getId(),
                r.getTitle(),
                r.getDescription(),
                r.getInstructions(),
                r.getCategory(),
                r.getImageUrl(),
                r.getPrepMinutes(),
                r.getServings(),
                r.isFavorite(),
                toNutritionDto(r.getNutrition()),
                r.getIngredients().stream().map(RecipeMapper::toIngredientDto).toList(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    public static IngredientDto toIngredientDto(Ingredient i) {
        return new IngredientDto(i.getId(), i.getName(), i.getAmount(), i.getUnit());
    }

    public static NutritionDto toNutritionDto(Nutrition n) {
        if (n == null) return null;
        return new NutritionDto(n.getCaloriesKcal(), n.getProteinG(), n.getFatG(), n.getCarbsG());
    }

    public static Nutrition toNutrition(NutritionDto dto) {
        if (dto == null) return null;
        Nutrition n = new Nutrition();
        n.setCaloriesKcal(dto.caloriesKcal());
        n.setProteinG(dto.proteinG());
        n.setFatG(dto.fatG());
        n.setCarbsG(dto.carbsG());
        return n;
    }

    public static List<Ingredient> toIngredients(List<IngredientDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(d -> new Ingredient(d.name(), d.amount(), d.unit()))
                .toList();
    }
}