package com.example.rezeptapp.dto;

public record NutritionDto(
        Integer caloriesKcal,
        Double proteinG,
        Double fatG,
        Double carbsG
) {}