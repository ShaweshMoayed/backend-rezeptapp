package com.example.rezeptapp.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "meal_plan_entries",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_plan_day_slot",
                columnNames = {"meal_plan_id", "plan_day", "slot"}
        )
)
public class MealPlanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Zugehöriger Plan
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id")
    private MealPlan plan;

    // Datum innerhalb der Woche (Mo–So)
    @Column(name = "plan_day", nullable = false)
    private LocalDate day;

    // Frühstück/Mittag/Abend
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealSlot slot;

    // ausgewähltes Rezept (darf null sein -> Slot "leer")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    // optional
    private Integer servings;

    public MealPlanEntry() {}

    public Long getId() { return id; }

    public MealPlan getPlan() { return plan; }
    public void setPlan(MealPlan plan) { this.plan = plan; }

    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }

    public MealSlot getSlot() { return slot; }
    public void setSlot(MealSlot slot) { this.slot = slot; }

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }

    public Integer getServings() { return servings; }
    public void setServings(Integer servings) { this.servings = servings; }
}