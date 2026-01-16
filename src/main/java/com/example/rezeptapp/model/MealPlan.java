package com.example.rezeptapp.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Transiente Klasse (kein @Entity).
 * Wird nur für Validierung + PDF-Erstellung genutzt.
 */
public class MealPlan {

    private String title;
    private LocalDate weekStartMonday;
    private List<MealPlanEntry> entries = new ArrayList<>();

    public MealPlan() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getWeekStartMonday() { return weekStartMonday; }
    public void setWeekStartMonday(LocalDate weekStartMonday) { this.weekStartMonday = weekStartMonday; }

    public List<MealPlanEntry> getEntries() { return entries; }
    public void setEntries(List<MealPlanEntry> entries) {
        this.entries = (entries == null) ? new ArrayList<>() : new ArrayList<>(entries);
    }
}