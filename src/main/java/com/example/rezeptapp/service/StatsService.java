package com.example.rezeptapp.service;

import com.example.rezeptapp.model.Nutrition;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final RecipeRepository recipeRepository;

    public StatsService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public record StatsRequest(List<Long> recipeIds) {}

    public record Macro(double caloriesKcal, double proteinG, double fatG, double carbsG) {}

    public record RecipeMacro(Long id, String title, Macro macro) {}

    public record StatsResponse(
            List<RecipeMacro> recipes,
            Macro total,
            Macro donut // für Donut (Makro-Verteilung) -> protein/fat/carbs, calories optional
    ) {}

    @Transactional(readOnly = true)
    public StatsResponse buildStats(StatsRequest req) {
        if (req == null || req.recipeIds() == null || req.recipeIds().isEmpty()) {
            throw new IllegalArgumentException("recipeIds darf nicht leer sein");
        }

        // Duplicates entfernen, Reihenfolge behalten
        List<Long> ids = req.recipeIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Recipe> found = recipeRepository.findAllById(ids);

        // Missing IDs check
        Set<Long> foundIds = found.stream().map(Recipe::getId).collect(Collectors.toSet());
        List<Long> missing = ids.stream().filter(id -> !foundIds.contains(id)).toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Recipe nicht gefunden: " + missing);
        }

        // wieder in request-Reihenfolge sortieren
        Map<Long, Recipe> byId = found.stream().collect(Collectors.toMap(Recipe::getId, r -> r));
        List<Recipe> ordered = ids.stream().map(byId::get).toList();

        List<RecipeMacro> perRecipe = ordered.stream()
                .map(r -> new RecipeMacro(r.getId(), r.getTitle(), toMacro(r.getNutrition())))
                .toList();

        Macro total = sum(perRecipe.stream().map(RecipeMacro::macro).toList());

        // donut: Protein/Fett/Carbs (Kalorien optional – wir geben sie mit, aber Frontend nutzt meistens nur macros)
        Macro donut = new Macro(
                total.caloriesKcal(),
                total.proteinG(),
                total.fatG(),
                total.carbsG()
        );

        return new StatsResponse(perRecipe, total, donut);
    }

    private Macro toMacro(Nutrition n) {
        if (n == null) return new Macro(0, 0, 0, 0);

        double calories = n.getCaloriesKcal() == null ? 0 : n.getCaloriesKcal();
        double protein = n.getProteinG() == null ? 0 : n.getProteinG();
        double fat = n.getFatG() == null ? 0 : n.getFatG();
        double carbs = n.getCarbsG() == null ? 0 : n.getCarbsG();

        return new Macro(calories, protein, fat, carbs);
    }

    private Macro sum(List<Macro> macros) {
        double c = 0, p = 0, f = 0, cb = 0;
        for (Macro m : macros) {
            c += m.caloriesKcal();
            p += m.proteinG();
            f += m.fatG();
            cb += m.carbsG();
        }
        return new Macro(c, p, f, cb);
    }
}