package com.example.rezeptapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_plans")
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String title;

    @Column(nullable = false)
    private LocalDate weekStartMonday;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private UserAccount user;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealPlanEntry> entries = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    public MealPlan() {}

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void setEntries(List<MealPlanEntry> newEntries) {
        this.entries.clear();
        if (newEntries != null) {
            for (MealPlanEntry e : newEntries) {
                e.setPlan(this);
                this.entries.add(e);
            }
        }
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getWeekStartMonday() { return weekStartMonday; }
    public void setWeekStartMonday(LocalDate weekStartMonday) { this.weekStartMonday = weekStartMonday; }

    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }

    public List<MealPlanEntry> getEntries() { return entries; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}