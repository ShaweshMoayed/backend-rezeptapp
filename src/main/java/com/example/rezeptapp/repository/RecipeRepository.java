package com.example.rezeptapp.repository;

import com.example.rezeptapp.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // für Seeder "Insert-if-missing"
    boolean existsByTitleIgnoreCase(String title);



    @Query("""
        SELECT r FROM Recipe r
        WHERE r.createdByUsername IS NULL
           OR lower(r.createdByUsername) = lower(:username)
        ORDER BY r.id DESC
        """)
    List<Recipe> findPublicOrOwned(@Param("username") String username);

    @Query("""
        SELECT r FROM Recipe r
        WHERE (r.createdByUsername IS NULL OR lower(r.createdByUsername) = lower(:username))
          AND (
                lower(r.title) LIKE lower(concat('%', :search, '%'))
             OR lower(r.description) LIKE lower(concat('%', :search, '%'))
          )
        ORDER BY r.id DESC
        """)
    List<Recipe> searchPublicOrOwned(
            @Param("username") String username,
            @Param("search") String search
    );

    @Query("""
        SELECT r FROM Recipe r
        WHERE (r.createdByUsername IS NULL OR lower(r.createdByUsername) = lower(:username))
          AND lower(r.category) = lower(:category)
        ORDER BY r.id DESC
        """)
    List<Recipe> findByCategoryPublicOrOwned(
            @Param("username") String username,
            @Param("category") String category
    );

    @Query("""
        SELECT r FROM Recipe r
        WHERE (r.createdByUsername IS NULL OR lower(r.createdByUsername) = lower(:username))
          AND lower(r.category) = lower(:category)
          AND (
                lower(r.title) LIKE lower(concat('%', :search, '%'))
             OR lower(r.description) LIKE lower(concat('%', :search, '%'))
          )
        ORDER BY r.id DESC
        """)
    List<Recipe> searchInCategoryPublicOrOwned(
            @Param("username") String username,
            @Param("category") String category,
            @Param("search") String search
    );

    @Query("""
        SELECT DISTINCT r.category
        FROM Recipe r
        WHERE r.category IS NOT NULL
          AND r.category <> ''
          AND (r.createdByUsername IS NULL OR lower(r.createdByUsername) = lower(:username))
        ORDER BY lower(r.category)
        """)
    List<String> findCategoriesPublicOrOwned(@Param("username") String username);

    // ============================
    // Mine-only (wie bisher)
    // ============================

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

    // ============================
    // Public-only (für Gäste)
    // ============================

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.createdByUsername IS NULL
        ORDER BY r.id DESC
        """)
    List<Recipe> findPublicOnly();

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.createdByUsername IS NULL
          AND (
                lower(r.title) LIKE lower(concat('%', :search, '%'))
             OR lower(r.description) LIKE lower(concat('%', :search, '%'))
          )
        ORDER BY r.id DESC
        """)
    List<Recipe> searchPublicOnly(@Param("search") String search);

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.createdByUsername IS NULL
          AND lower(r.category) = lower(:category)
        ORDER BY r.id DESC
        """)
    List<Recipe> findByCategoryPublicOnly(@Param("category") String category);

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.createdByUsername IS NULL
          AND lower(r.category) = lower(:category)
          AND (
                lower(r.title) LIKE lower(concat('%', :search, '%'))
             OR lower(r.description) LIKE lower(concat('%', :search, '%'))
          )
        ORDER BY r.id DESC
        """)
    List<Recipe> searchInCategoryPublicOnly(
            @Param("category") String category,
            @Param("search") String search
    );

    @Query("""
        SELECT DISTINCT r.category
        FROM Recipe r
        WHERE r.category IS NOT NULL
          AND r.category <> ''
          AND r.createdByUsername IS NULL
        ORDER BY lower(r.category)
        """)
    List<String> findCategoriesPublicOnly();
}