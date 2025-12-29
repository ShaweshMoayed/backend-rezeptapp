package com.example.rezeptapp.repository;

import com.example.rezeptapp.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("""
        select r
        from Recipe r
        where (:favorite is null or r.favorite = :favorite)
          and (:category is null or :category = '' or lower(r.category) = lower(:category))
          and (
                :search is null or :search = ''
                or lower(r.title) like lower(concat('%', :search, '%'))
                or lower(r.description) like lower(concat('%', :search, '%'))
          )
        order by r.createdAt desc
    """)
    List<Recipe> search(
            @Param("search") String search,
            @Param("category") String category,
            @Param("favorite") Boolean favorite
    );
}