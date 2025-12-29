package com.example.rezeptapp.repository;

import com.example.rezeptapp.model.Recipe;
import org.springframework.data.jpa.domain.Specification;

public class RecipeSpecifications {

    public static Specification<Recipe> titleOrDescriptionContains(String q) {
        return (root, query, cb) -> {
            String like = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }

    public static Specification<Recipe> hasCategory(String category) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    public static Specification<Recipe> isFavorite(boolean fav) {
        return (root, query, cb) -> cb.equal(root.get("favorite"), fav);
    }
}