package com.example.rezeptapp.dto;

import jakarta.validation.constraints.NotBlank;

public record IngredientDto(
        Long id,
        @NotBlank String name,
        String amount,
        String unit
) {}