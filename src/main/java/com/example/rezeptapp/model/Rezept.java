package com.example.rezeptapp.model;

public class Rezept {
    private Long id;
    private String name;
    private String beschreibung;

    public Rezept(Long id, String name, String beschreibung) {
        this.id = id;
        this.name = name;
        this.beschreibung = beschreibung;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBeschreibung() { return beschreibung; }
}