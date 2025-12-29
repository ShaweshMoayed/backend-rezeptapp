package com.example.rezeptapp.controller;

import com.example.rezeptapp.dto.RecipeCreateRequest;
import com.example.rezeptapp.dto.RecipeResponse;
import com.example.rezeptapp.dto.RecipeUpdateRequest;
import com.example.rezeptapp.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService service;

    public RecipeController(RecipeService service) {
        this.service = service;
    }

    // GET /api/recipes?search=...&category=...&favorite=true
    @GetMapping
    public List<RecipeResponse> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean favorite
    ) {
        return service.findAll(search, category, favorite);
    }

    // GET /api/recipes/{id}
    @GetMapping("/{id}")
    public RecipeResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    // POST /api/recipes
    @PostMapping
    public RecipeResponse create(@Valid @RequestBody RecipeCreateRequest request) {
        return service.create(request);
    }

    // PUT /api/recipes/{id}
    @PutMapping("/{id}")
    public RecipeResponse update(@PathVariable Long id, @Valid @RequestBody RecipeUpdateRequest request) {
        return service.update(id, request);
    }

    // DELETE /api/recipes/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}