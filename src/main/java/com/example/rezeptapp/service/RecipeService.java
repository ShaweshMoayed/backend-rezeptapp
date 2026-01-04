package com.example.rezeptapp.service;

import com.example.rezeptapp.model.Ingredient;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.model.UserAccount;
import com.example.rezeptapp.repository.RecipeRepository;
import com.example.rezeptapp.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeService {

    private static final String MINE_VALUE = "__mine__";

    private final RecipeRepository repo;
    private final UserAccountRepository userRepo;

    public RecipeService(RecipeRepository repo, UserAccountRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    public List<Recipe> findAll(String search, String category) {
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        // ✅ Schutz: "__mine__" darf hier NICHT als normale Kategorie laufen
        if (hasCategory && category.trim().equalsIgnoreCase(MINE_VALUE)) {
            throw new IllegalArgumentException("unauthorized");
        }

        if (!hasSearch && !hasCategory) return repo.findAll();

        if (hasCategory && hasSearch) {
            return repo.searchInCategory(category.trim(), search.trim());
        }
        if (hasCategory) return repo.findByCategoryIgnoreCase(category.trim());

        return repo.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search.trim(), search.trim());
    }

    /**
     * ✅ Eigene Rezepte (createdByUsername) optional mit Suche
     */
    public List<Recipe> findMine(String username, String search) {
        String u = username == null ? "" : username.trim();
        if (u.isBlank()) throw new IllegalArgumentException("unauthorized");

        boolean hasSearch = search != null && !search.isBlank();
        if (!hasSearch) {
            return repo.findByCreatedByUsernameIgnoreCase(u);
        }
        return repo.searchMine(u, search.trim());
    }

    public Recipe findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe nicht gefunden: " + id));
    }

    /**
     * ✅ CREATE: Pflicht prüfen + createdByUsername setzen
     */
    @Transactional
    public Recipe createForUser(Recipe recipe, String username) {
        String title = recipe.getTitle() == null ? "" : recipe.getTitle().trim();
        String desc = recipe.getDescription() == null ? "" : recipe.getDescription().trim();
        String instr = recipe.getInstructions() == null ? "" : recipe.getInstructions().trim();

        if (title.isBlank()) throw new IllegalArgumentException("Titel ist ein Pflichtfeld.");
        if (desc.isBlank()) throw new IllegalArgumentException("Beschreibung ist ein Pflichtfeld.");
        if (instr.isBlank()) throw new IllegalArgumentException("Mindestens 1 Schritt ist Pflicht.");

        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Mindestens 1 Zutat ist Pflicht.");
        }

        if (recipe.getNutrition() == null) {
            throw new IllegalArgumentException("Nährwerte sind Pflicht.");
        }

        recipe.setCreatedByUsername(username);

        if (recipe.getIngredients() != null) {
            for (Ingredient ing : recipe.getIngredients()) {
                ing.setRecipe(recipe);
            }
        }

        return repo.save(recipe);
    }

    /**
     * ✅ UPDATE: nur Owner darf ändern
     * - KEIN @Valid: tolerant / partial update
     */
    @Transactional
    public Recipe updateForUser(Long id, Recipe incoming, String username) {
        Recipe existing = findById(id);
        requireOwner(existing, username, "ändern");

        // partial update
        if (incoming.getTitle() != null) existing.setTitle(incoming.getTitle());
        if (incoming.getDescription() != null) existing.setDescription(incoming.getDescription());
        if (incoming.getInstructions() != null) existing.setInstructions(incoming.getInstructions());
        if (incoming.getCategory() != null) existing.setCategory(incoming.getCategory());

        if (incoming.getImageUrl() != null) existing.setImageUrl(incoming.getImageUrl());
        if (incoming.getImageBase64() != null) existing.setImageBase64(incoming.getImageBase64());

        if (incoming.getPrepMinutes() != null) existing.setPrepMinutes(incoming.getPrepMinutes());
        if (incoming.getServings() != null) existing.setServings(incoming.getServings());

        if (incoming.getNutrition() != null) existing.setNutrition(incoming.getNutrition());
        if (incoming.getIngredients() != null) existing.setIngredients(incoming.getIngredients());

        return repo.save(existing);
    }

    /**
     * ✅ DELETE: nur Owner darf löschen
     */
    @Transactional
    public void deleteForUser(Long id, String username) {
        Recipe existing = findById(id);
        requireOwner(existing, username, "löschen");

        repo.delete(existing);
    }

    /**
     * Owner-Check:
     * - Wenn createdByUsername fehlt (Seeder/alte Rezepte) => wir erlauben NICHT, dass beliebige User sie ändern/löschen.
     */
    private void requireOwner(Recipe recipe, String username, String actionVerb) {
        String owner = recipe.getCreatedByUsername();
        String u = username == null ? "" : username.trim();

        if (u.isBlank()) throw new IllegalArgumentException("forbidden");

        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("forbidden: Dieses Rezept hat keinen Besitzer und kann nicht " + actionVerb + " werden.");
        }

        if (!owner.equalsIgnoreCase(u)) {
            throw new IllegalArgumentException("forbidden: Nur der Ersteller darf dieses Rezept " + actionVerb + ".");
        }
    }

    // ===== Favoriten pro User =====

    @Transactional(readOnly = true)
    public List<Recipe> getFavorites(UserAccount user) {
        return new ArrayList<>(user.getFavorites());
    }

    @Transactional(readOnly = true)
    public List<Long> getFavoriteIds(UserAccount user) {
        return user.getFavorites().stream().map(Recipe::getId).toList();
    }

    @Transactional
    public void addFavorite(UserAccount user, Long recipeId) {
        Recipe recipe = findById(recipeId);
        user.getFavorites().add(recipe);
        userRepo.save(user);
    }

    @Transactional
    public void removeFavorite(UserAccount user, Long recipeId) {
        Recipe recipe = findById(recipeId);
        user.getFavorites().remove(recipe);
        userRepo.save(user);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return repo.findAllCategories().stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}