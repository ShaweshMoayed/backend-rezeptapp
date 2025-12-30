package com.example.rezeptapp.controller;

import com.example.rezeptapp.service.StatsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rezeptapp/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // POST /rezeptapp/stats
    // Body: { "recipeIds": [1,2,3] }
    @PostMapping
    public ResponseEntity<StatsService.StatsResponse> stats(@RequestBody StatsService.StatsRequest req) {
        return ResponseEntity.ok(statsService.buildStats(req));
    }
}