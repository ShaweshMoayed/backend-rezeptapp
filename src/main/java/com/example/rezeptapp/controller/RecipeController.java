package com.example.rezeptapp.controller;

import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.model.UserAccount;
import com.example.rezeptapp.service.AuthService;
import com.example.rezeptapp.service.PdfService;
import com.example.rezeptapp.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/rezeptapp")
public class RecipeController {

    private final RecipeService recipeService;
    private final PdfService pdfService;
    private final AuthService authService;

    public RecipeController(RecipeService recipeService, PdfService pdfService, AuthService authService) {
        this.recipeService = recipeService;
        this.pdfService = pdfService;
        this.authService = authService;
    }

    @GetMapping
    public List<Recipe> getAllRecipes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category
    ) {
        return recipeService.findAll(search, category);
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return recipeService.getAllCategories();
    }

    @GetMapping("/{id}")
    public Recipe getRecipeById(@PathVariable Long id) {
        return recipeService.findById(id);
    }

    @PostMapping
    public Recipe createRecipe(@Valid @RequestBody Recipe recipe) {
        return recipeService.create(recipe);
    }

    @PutMapping("/{id}")
    public Recipe updateRecipe(@PathVariable Long id, @Valid @RequestBody Recipe recipe) {
        return recipeService.update(id, recipe);
    }

    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable Long id) {
        recipeService.delete(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Recipe recipe = recipeService.findById(id);
        byte[] pdf = pdfService.createRecipePdf(recipe);

        String filename = (recipe.getTitle() == null ? "rezept" : recipe.getTitle())
                .replaceAll("[^a-zA-Z0-9\\-_ ]", "")
                .trim()
                .replace(" ", "_");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodeFilename(filename + ".pdf"))
                .body(pdf);
    }

    // ===== Favoriten =====

    @GetMapping("/favorites")
    public List<Recipe> getMyFavorites(@RequestHeader("Authorization") String authHeader) {
        UserAccount user = requireUserFromHeader(authHeader);
        return recipeService.getFavorites(user);
    }

    @GetMapping("/favorites/ids")
    public List<Long> getMyFavoriteIds(@RequestHeader("Authorization") String authHeader) {
        UserAccount user = requireUserFromHeader(authHeader);
        return recipeService.getFavoriteIds(user);
    }

    @PostMapping("/{id}/favorite")
    public void addFavorite(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        UserAccount user = requireUserFromHeader(authHeader);
        recipeService.addFavorite(user, id);
    }

    @DeleteMapping("/{id}/favorite")
    public void removeFavorite(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        UserAccount user = requireUserFromHeader(authHeader);
        recipeService.removeFavorite(user, id);
    }

    private UserAccount requireUserFromHeader(String authHeader) {
        try {
            String token = tokenFromHeader(authHeader);
            return authService.requireUser(token);
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "unauthorized");
        }
    }

    private String tokenFromHeader(String authHeader) {
        if (authHeader == null) throw new IllegalArgumentException("unauthorized");
        String prefix = "Bearer ";
        if (!authHeader.startsWith(prefix)) throw new IllegalArgumentException("unauthorized");
        return authHeader.substring(prefix.length()).trim();
    }

    private String encodeFilename(String name) {
        return java.net.URLEncoder.encode(name, StandardCharsets.UTF_8);
    }
}