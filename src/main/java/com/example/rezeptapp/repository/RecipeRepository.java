package com.example.rezeptapp.repository;

import com.example.rezeptapp.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // ✅ für Seeder "Insert-if-missing"
    boolean existsByTitleIgnoreCase(String title);

    List<Recipe> findByCategoryIgnoreCase(String category);

    List<Recipe> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title,
            String description
    );

    @Query("""
        SELECT r FROM Recipe r
        WHERE lower(r.category) = lower(:category)
          AND (
                lower(r.title) LIKE lower(concat('%', :search, '%'))
             OR lower(r.description) LIKE lower(concat('%', :search, '%'))
          )
        """)
    List<Recipe> searchInCategory(
            @Param("category") String category,
            @Param("search") String search
    );

    // Eigene Rezepte
    List<Recipe> findByCreatedByUsernameIgnoreCase(String createdByUsername);

    @Query("""
        SELECT r FROM Recipe r
        WHERE lower(r.createdByUsername) = lower(:username)
          AND (
                lower(r.title) LIKE lower(concat('%', :search, '%'))
             OR lower(r.description) LIKE lower(concat('%', :search, '%'))
          )
        """)
    List<Recipe> searchMine(
            @Param("username") String username,
            @Param("search") String search
    );

    @Query("""
        SELECT r.category
        FROM Recipe r
        WHERE r.category IS NOT NULL
          AND r.category <> ''
        GROUP BY r.category
        ORDER BY lower(r.category)
        """)
    List<String> findAllCategories();
}