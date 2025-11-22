package com.example.rezeptapp.controller;

import com.example.rezeptapp.model.Rezept;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(
        origins = {
                "http://localhost:5173",                // Vue-Dev-Server lokal
                "https://frontend-rezeptapp.onrender.com" // TODO: hier später deine echte Render-URL eintragen
        }
)
public class RezeptController {

    // GET /rezepte -> Liste von Rezepten als JSON
    @GetMapping("/rezepte")
    public List<Rezept> getRezepte() {
        return List.of(
                new Rezept(
                        1L,
                        "Spaghetti Carbonara",
                        "Klassisches italienisches Gericht mit Speck, Ei und Parmesan."
                ),
                new Rezept(
                        2L,
                        "Gemüsesuppe",
                        "Leichte Suppe mit frischem Gemüse – perfekt für kalte Tage."
                ),
                new Rezept(
                        3L,
                        "Pancakes",
                        "Fluffige Pfannkuchen mit Ahornsirup und Beeren."
                )
        );
    }
}
