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

    public List<Recipe> findAll(String usernameOrNull, String search, String category) {
        boolean loggedIn = usernameOrNull != null && !usernameOrNull.trim().isBlank();
        String u = loggedIn ? usernameOrNull.trim() : "";

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (hasCategory && category.trim().equalsIgnoreCase(MINE_VALUE)) {
            throw new IllegalArgumentException("unauthorized");
        }

        if (loggedIn) {
            if (!hasSearch && !hasCategory) return repo.findPublicOrOwned(u);

            if (hasCategory && hasSearch) return repo.searchInCategoryPublicOrOwned(u, category.trim(), search.trim());
            if (hasCategory) return repo.findByCategoryPublicOrOwned(u, category.trim());

            return repo.searchPublicOrOwned(u, search.trim());
        }

        // guest => public only
        if (!hasSearch && !hasCategory) return repo.findPublicOnly();

        if (hasCategory && hasSearch) return repo.searchInCategoryPublicOnly(category.trim(), search.trim());
        if (hasCategory) return repo.findByCategoryPublicOnly(category.trim());

        return repo.searchPublicOnly(search.trim());
    }

    public List<Recipe> findMine(String username, String search) {
        String u = username == null ? "" : username.trim();
        if (u.isBlank()) throw new IllegalArgumentException("unauthorized");

        boolean hasSearch = search != null && !search.isBlank();
        if (!hasSearch) return repo.findMineOrdered(u);

        return repo.searchMineOrdered(u, search.trim());
    }

    public Recipe findByIdForUser(Long id, String usernameOrNull) {
        boolean loggedIn = usernameOrNull != null && !usernameOrNull.trim().isBlank();
        String u = loggedIn ? usernameOrNull.trim() : "";

        if (!loggedIn) {
            return repo.findPublicById(id)
                    .orElseThrow(() -> new IllegalArgumentException("forbidden"));
        }

        return repo.findVisibleByIdForUser(id, u)
                .orElseThrow(() -> new IllegalArgumentException("forbidden"));
    }

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

    @Transactional
    public Recipe updateForUser(Long id, Recipe incoming, String username) {
        Recipe existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe nicht gefunden: " + id));

        requireOwner(existing, username, "ändern");

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

    @Transactional
    public void deleteForUser(Long id, String username) {
        Recipe existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe nicht gefunden: " + id));

        requireOwner(existing, username, "löschen");
        repo.delete(existing);
    }

    private void requireOwner(Recipe recipe, String username, String actionVerb) {
        String owner = recipe.getCreatedByUsername();
        String u = username == null ? "" : username.trim();

        if (u.isBlank()) throw new IllegalArgumentException("forbidden");

        // Seeder/public Rezepte: NIE edit/delete
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("forbidden: Dieses Rezept hat keinen Besitzer und kann nicht " + actionVerb + " werden.");
        }

        if (!owner.equalsIgnoreCase(u)) {
            throw new IllegalArgumentException("forbidden: Nur der Ersteller darf dieses Rezept " + actionVerb + ".");
        }
    }

    // ===== Favoriten =====

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
        Recipe recipe = findByIdForUser(recipeId, user.getUsername());
        user.getFavorites().add(recipe);
        userRepo.save(user);
    }

    @Transactional
    public void removeFavorite(UserAccount user, Long recipeId) {
        Recipe recipe = repo.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe nicht gefunden: " + recipeId));
        user.getFavorites().remove(recipe);
        userRepo.save(user);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories(String usernameOrNull) {
        boolean loggedIn = usernameOrNull != null && !usernameOrNull.trim().isBlank();
        String u = loggedIn ? usernameOrNull.trim() : "";

        List<String> raw = loggedIn
                ? repo.findCategoriesPublicOrOwned(u)
                : repo.findCategoriesPublicOnly();

        return raw.stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}