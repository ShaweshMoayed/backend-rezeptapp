package com.example.rezeptapp.service;

import com.example.rezeptapp.dto.RecipeCreateRequest;
import com.example.rezeptapp.dto.RecipeResponse;
import com.example.rezeptapp.dto.RecipeUpdateRequest;
import com.example.rezeptapp.mapper.RecipeMapper;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.repository.RecipeRepository;
import com.example.rezeptapp.repository.RecipeSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository repo;

    public RecipeService(RecipeRepository repo) {
        this.repo = repo;
    }

    public List<RecipeResponse> findAll(String search, String category, Boolean favorite) {
        Specification<Recipe> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and(RecipeSpecifications.titleOrDescriptionContains(search));
        }
        if (category != null && !category.isBlank()) {
            spec = spec.and(RecipeSpecifications.hasCategory(category));
        }
        if (favorite != null) {
            spec = spec.and(RecipeSpecifications.isFavorite(favorite));
        }

        return repo.findAll(spec).stream()
                .map(RecipeMapper::toResponse)
                .toList();
    }

    public RecipeResponse findById(Long id) {
        Recipe r = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe nicht gefunden: " + id));
        return RecipeMapper.toResponse(r);
    }

    @Transactional
    public RecipeResponse create(RecipeCreateRequest req) {
        Recipe r = new Recipe();
        r.setTitle(req.title());
        r.setDescription(req.description());
        r.setInstructions(req.instructions());
        r.setCategory(req.category());
        r.setImageUrl(req.imageUrl());
        r.setPrepMinutes(req.prepMinutes());
        r.setServings(req.servings());
        r.setFavorite(req.favorite() != null && req.favorite());
        r.setNutrition(RecipeMapper.toNutrition(req.nutrition()));
        r.setIngredients(RecipeMapper.toIngredients(req.ingredients()));

        Recipe saved = repo.save(r);
        return RecipeMapper.toResponse(saved);
    }

    @Transactional
    public RecipeResponse update(Long id, RecipeUpdateRequest req) {
        Recipe r = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe nicht gefunden: " + id));

        if (req.title() != null) r.setTitle(req.title());
        if (req.description() != null) r.setDescription(req.description());
        if (req.instructions() != null) r.setInstructions(req.instructions());
        if (req.category() != null) r.setCategory(req.category());
        if (req.imageUrl() != null) r.setImageUrl(req.imageUrl());
        if (req.prepMinutes() != null) r.setPrepMinutes(req.prepMinutes());
        if (req.servings() != null) r.setServings(req.servings());
        if (req.favorite() != null) r.setFavorite(req.favorite());
        if (req.nutrition() != null) r.setNutrition(RecipeMapper.toNutrition(req.nutrition()));

        // Zutaten: wenn übergeben, ersetzen wir komplett (simpel & zuverlässig)
        if (req.ingredients() != null) {
            r.setIngredients(RecipeMapper.toIngredients(req.ingredients()));
        }

        return RecipeMapper.toResponse(repo.save(r));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Recipe nicht gefunden: " + id);
        }
        repo.deleteById(id);
    }
}