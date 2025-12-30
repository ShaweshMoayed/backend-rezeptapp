package com.example.rezeptapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Pflicht (wenig & sinnvoll)
    @NotBlank(message = "title darf nicht leer sein")
    @Size(min = 2, max = 120, message = "title muss 2-120 Zeichen haben")
    @Column(nullable = false, length = 120)
    private String title;

    // ✅ Pflicht (wenig & sinnvoll)
    @NotBlank(message = "description darf nicht leer sein")
    @Size(min = 3, max = 2000, message = "description muss 3-2000 Zeichen haben")
    @Column(nullable = false, length = 2000)
    private String description;

    // optional
    @Column(length = 10000)
    private String instructions;

    // optional
    private String category;

    // optional: externer Link
    @Column(length = 20000)
    private String imageUrl;

    // optional: Base64 ("data:image/...;base64,..." oder nur base64)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String imageBase64;

    // optional
    private Integer prepMinutes;
    private Integer servings;

    @Embedded
    private Nutrition nutrition;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    public Recipe() {}

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // Zutaten sauber setzen (Back-Reference recipe_id)
    public void setIngredients(List<Ingredient> newIngredients) {
        this.ingredients.clear();
        if (newIngredients != null) {
            for (Ingredient ing : newIngredients) {
                ing.setRecipe(this);
                this.ingredients.add(ing);
            }
        }
    }

    // ===== Getter / Setter =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public Integer getPrepMinutes() { return prepMinutes; }
    public void setPrepMinutes(Integer prepMinutes) { this.prepMinutes = prepMinutes; }

    public Integer getServings() { return servings; }
    public void setServings(Integer servings) { this.servings = servings; }

    public Nutrition getNutrition() { return nutrition; }
    public void setNutrition(Nutrition nutrition) { this.nutrition = nutrition; }

    public List<Ingredient> getIngredients() { return ingredients; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // wichtig für Set<Recipe> favorites
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recipe other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}