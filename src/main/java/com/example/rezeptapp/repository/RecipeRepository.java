package com.example.rezeptapp.repository;

import com.example.rezeptapp.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // für Seeder "Insert-if-missing"
    boolean existsByTitleIgnoreCase(String title);

    // =========================================================
    // Sorting: Seeder (createdByUsername IS NULL) zuerst,
    // dann User-Rezepte; innerhalb jeweils ID ASC (wie früher)
    // =========================================================

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.createdByUsername IS NULL
           OR lower(r.createdByUsername) = lower(:username)
        ORDER BY
          CASE WHEN r.createdByUsername IS NULL THEN 0 ELSE 1 END ASC,
          r.id ASC
        """)
    List<Recipe> findPublicOrOwned(@Param("username") String username);

    @Query("""
        SELECT r FROM Recipe r
        WHERE (r.createdByUsername IS NULL OR lower(r.createdByUsername) = lower(:username))
          AND (
                lower(r.title) LIKE lower(concat('%', :search, '%'))
             OR lower(r.description) LIKE lower(concat('%', :search, '%'))
          )
        ORDER BY
          CASE WHEN r.createdByUsername IS NULL THEN 0 ELSE 1 END ASC,
          r.id ASC
        """)
    List<Recipe> searchPublicOrOwned(
            @Param("username") String username,
            @Param("search") String search
    );

    @Query("""
        SELECT r FROM Recipe r
        WHERE (r.createdByUsername IS NULL OR lower(r.createdByUsername) = lower(:username))
          AND lower(r.category) = lower(:category)
        ORDER BY
          CASE WHEN r.createdByUsername IS NULL THEN 0 ELSE 1 END ASC,
          r.id ASC
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
        ORDER BY
          CASE WHEN r.createdByUsername IS NULL THEN 0 ELSE 1 END ASC,
          r.id ASC
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

    // =========================================================
    // Mine-only (Eigene Rezepte) -> nur owned, ebenfalls ASC
    // =========================================================

    @Query("""
        SELECT r FROM Recipe r
        WHERE lower(r.createdByUsername) = lower(:username)
        ORDER BY r.id ASC
        """)
    List<Recipe> findMineOrdered(@Param("username") String username);

    @Query("""
        SELECT r FROM Recipe r
        WHERE lower(r.createdByUsername) = lower(:username)
          AND (
                lower(r.title) LIKE lower(concat('%', :search, '%'))
             OR lower(r.description) LIKE lower(concat('%', :search, '%'))
          )
        ORDER BY r.id ASC
        """)
    List<Recipe> searchMineOrdered(
            @Param("username") String username,
            @Param("search") String search
    );

    // =========================================================
    // Public-only (für Gäste) -> ASC
    // =========================================================

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.createdByUsername IS NULL
        ORDER BY r.id ASC
        """)
    List<Recipe> findPublicOnly();

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.createdByUsername IS NULL
          AND (
                lower(r.title) LIKE lower(concat('%', :search, '%'))
             OR lower(r.description) LIKE lower(concat('%', :search, '%'))
          )
        ORDER BY r.id ASC
        """)
    List<Recipe> searchPublicOnly(@Param("search") String search);

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.createdByUsername IS NULL
          AND lower(r.category) = lower(:category)
        ORDER BY r.id ASC
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
        ORDER BY r.id ASC
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

    // =========================================================
    // Sichtbarkeit Detail: public ODER own
    // (damit niemand fremde private Rezepte per URL sehen kann)
    // =========================================================

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.id = :id
          AND (r.createdByUsername IS NULL OR lower(r.createdByUsername) = lower(:username))
        """)
    Optional<Recipe> findVisibleByIdForUser(
            @Param("id") Long id,
            @Param("username") String username
    );

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.id = :id
          AND r.createdByUsername IS NULL
        """)
    Optional<Recipe> findPublicById(@Param("id") Long id);
}