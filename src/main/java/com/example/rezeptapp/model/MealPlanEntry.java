package com.example.rezeptapp.model;

import java.time.LocalDate;

/**
 * Transiente Klasse (kein @Entity).
 * Wird nur für Validierung + PDF-Erstellung genutzt.
 */
public class MealPlanEntry {

    private LocalDate day;
    private MealSlot slot;

    // darf null sein => Slot leer
    private Recipe recipe;

    // optional
    private Integer servings;

    public MealPlanEntry() {}

    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }

    public MealSlot getSlot() { return slot; }
    public void setSlot(MealSlot slot) { this.slot = slot; }

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }

    public Integer getServings() { return servings; }
    public void setServings(Integer servings) { this.servings = servings; }
}