package com.example.rezeptapp.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Nutrition {
    private Integer caloriesKcal;
    private Double proteinG;
    private Double fatG;
    private Double carbsG;

    public Nutrition() {}

    public Integer getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(Integer caloriesKcal) { this.caloriesKcal = caloriesKcal; }

    public Double getProteinG() { return proteinG; }
    public void setProteinG(Double proteinG) { this.proteinG = proteinG; }

    public Double getFatG() { return fatG; }
    public void setFatG(Double fatG) { this.fatG = fatG; }

    public Double getCarbsG() { return carbsG; }
    public void setCarbsG(Double carbsG) { this.carbsG = carbsG; }
}