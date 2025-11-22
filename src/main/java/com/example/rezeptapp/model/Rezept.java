package com.example.rezeptapp.model;

public class Rezept {

    private Long id;
    private String name;
    private String beschreibung;

    // ️ WICHTIG: Leerer Konstruktor für JSON (und später JPA)
    public Rezept() {
    }

    public Rezept(Long id, String name, String beschreibung) {
        this.id = id;
        this.name = name;
        this.beschreibung = beschreibung;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }
}
