package com.example.rezeptapp.controller;

import com.example.rezeptapp.model.Rezept;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class RezeptController {

    @GetMapping("/rezepte")
    public List<Rezept> getRezepte() {
        return List.of(
                new Rezept(1L, "Spaghetti Carbonara", "Klassisches italienisches Gericht."),
                new Rezept(2L, "Gemüsesuppe", "Leichte Suppe mit frischem Gemüse.")
        );
    }
}