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

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/rezeptapp")
public class RecipeController {

    private static final String MINE_VALUE = "__mine__";

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
            @RequestParam(required = false) String category,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // "Eigene Rezepte" special-case
        if (category != null && category.trim().equalsIgnoreCase(MINE_VALUE)) {
            UserAccount user = requireUserFromHeader(authHeader);
            return recipeService.findMine(user.getUsername(), search);
        }

        // Normal: public + (wenn eingeloggt) own private
        String usernameOrNull = tryUsernameFromHeader(authHeader);
        return recipeService.findAll(usernameOrNull, search, category);
    }

    @GetMapping("/categories")
    public List<String> getCategories(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String usernameOrNull = tryUsernameFromHeader(authHeader);
        return recipeService.getAllCategories(usernameOrNull);
    }

    @GetMapping("/{id}")
    public Recipe getRecipeById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String usernameOrNull = tryUsernameFromHeader(authHeader);

        try {
            return recipeService.findByIdForUser(id, usernameOrNull);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (msg.contains("forbidden")) {
                throw new ResponseStatusException(FORBIDDEN, "forbidden");
            }
            throw ex;
        }
    }

    /**
     * CREATE: nur eingeloggt + @Valid aktiv
     */
    @PostMapping
    public Recipe createRecipe(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody Recipe recipe
    ) {
        UserAccount user = requireUserFromHeader(authHeader);
        return recipeService.createForUser(recipe, user.getUsername());
    }

    /**
     * UPDATE: Login + nur Owner
     */
    @PutMapping("/{id}")
    public Recipe updateRecipe(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Recipe recipe
    ) {
        UserAccount user = requireUserFromHeader(authHeader);

        try {
            return recipeService.updateForUser(id, recipe, user.getUsername());
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (msg.contains("forbidden")) {
                throw new ResponseStatusException(FORBIDDEN, ex.getMessage());
            }
            throw ex;
        }
    }

    /**
     * DELETE: Login + nur Owner
     */
    @DeleteMapping("/{id}")
    public void deleteRecipe(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        UserAccount user = requireUserFromHeader(authHeader);

        try {
            recipeService.deleteForUser(id, user.getUsername());
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (msg.contains("forbidden")) {
                throw new ResponseStatusException(FORBIDDEN, ex.getMessage());
            }
            throw ex;
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String usernameOrNull = tryUsernameFromHeader(authHeader);

        // PDF nur, wenn man das Rezept sehen darf
        Recipe recipe = recipeService.findByIdForUser(id, usernameOrNull);

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
    public List<Recipe> getMyFavorites(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        UserAccount user = requireUserFromHeader(authHeader);
        return recipeService.getFavorites(user);
    }

    @GetMapping("/favorites/ids")
    public List<Long> getMyFavoriteIds(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        UserAccount user = requireUserFromHeader(authHeader);
        return recipeService.getFavoriteIds(user);
    }

    @PostMapping("/{id}/favorite")
    public void addFavorite(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        UserAccount user = requireUserFromHeader(authHeader);
        recipeService.addFavorite(user, id);
    }

    @DeleteMapping("/{id}/favorite")
    public void removeFavorite(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        UserAccount user = requireUserFromHeader(authHeader);
        recipeService.removeFavorite(user, id);
    }

    // Auth helpers

    private UserAccount requireUserFromHeader(String authHeader) {
        try {
            String token = tokenFromHeader(authHeader);
            return authService.requireUser(token);
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "unauthorized");
        }
    }

    /**
     *  Optional: gibt username zurück oder null (wenn kein/ungültiger Header)
     */
    private String tryUsernameFromHeader(String authHeader) {
        try {
            if (authHeader == null) return null;
            String token = tokenFromHeader(authHeader);
            return authService.requireUser(token).getUsername();
        } catch (Exception e) {
            return null;
        }
    }

    private String tokenFromHeader(String authHeader) {
        if (authHeader == null) throw new IllegalArgumentException("unauthorized");

        String prefix = "Bearer ";
        if (!authHeader.startsWith(prefix)) throw new IllegalArgumentException("unauthorized");

        String token = authHeader.substring(prefix.length()).trim();
        if (token.isEmpty()) throw new IllegalArgumentException("unauthorized");

        return token;
    }

    private String encodeFilename(String name) {
        return java.net.URLEncoder.encode(name, StandardCharsets.UTF_8);
    }
}