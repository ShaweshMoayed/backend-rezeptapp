package com.example.rezeptapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rezepte")
public class Rezept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String beschreibung;

    // ✅ Pflicht: leerer Konstruktor für JPA
    public Rezept() {
    }

    // Optionaler Komfort-Konstruktor (ohne id, weil DB die id vergibt)
    public Rezept(String name, String beschreibung) {
        this.name = name;
        this.beschreibung = beschreibung;
    }

    // Getter/Setter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }
}