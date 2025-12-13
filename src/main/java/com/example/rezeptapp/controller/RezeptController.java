package com.example.rezeptapp.controller;

import com.example.rezeptapp.model.Rezept;
import com.example.rezeptapp.service.RezeptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rezeptapp")
public class RezeptController {

    private final RezeptService rezeptService;

    public RezeptController(RezeptService rezeptService) {
        this.rezeptService = rezeptService;
    }

    // ✅ GET /rezeptapp
    @GetMapping
    public List<Rezept> getAlleRezepte() {
        return rezeptService.findAll();
    }

    // ✅ POST /rezeptapp
    @PostMapping
    public Rezept createRezept(@RequestBody Rezept rezept) {
        return rezeptService.create(rezept);
    }
}