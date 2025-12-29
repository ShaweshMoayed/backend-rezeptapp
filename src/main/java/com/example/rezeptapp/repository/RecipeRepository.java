package com.example.rezeptapp.repository;

import com.example.rezeptapp.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByCategoryIgnoreCase(String category);

    List<Recipe> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    List<Recipe> findByCategoryIgnoreCaseAndTitleContainingIgnoreCaseOrCategoryIgnoreCaseAndDescriptionContainingIgnoreCase(
            String category1, String title,
            String category2, String description
    );
}