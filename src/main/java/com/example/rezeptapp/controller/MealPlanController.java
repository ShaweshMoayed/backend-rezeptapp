package com.example.rezeptapp.controller;

import com.example.rezeptapp.model.MealPlan;
import com.example.rezeptapp.model.MealPlanEntry;
import com.example.rezeptapp.model.MealSlot;
import com.example.rezeptapp.model.UserAccount;
import com.example.rezeptapp.service.AuthService;
import com.example.rezeptapp.service.MealPlanPdfService;
import com.example.rezeptapp.service.MealPlanService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/rezeptapp/plans")
public class MealPlanController {

    private final MealPlanService planService;
    private final MealPlanPdfService planPdfService;
    private final AuthService authService;

    public MealPlanController(MealPlanService planService, MealPlanPdfService planPdfService, AuthService authService) {
        this.planService = planService;
        this.planPdfService = planPdfService;
        this.authService = authService;
    }

    // ===== DTOs =====
    public record PlanEntryDto(LocalDate day, MealSlot slot, Long recipeId, Integer servings) {}
    public record PlanRequest(String title, LocalDate weekStartMonday, List<PlanEntryDto> entries) {}

    // ===== NEW: PDF without saving =====
    // POST /rezeptapp/plans/pdf   Body: {title, weekStartMonday, entries}  -> application/pdf
    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody PlanRequest req
    ) {
        UserAccount user = requireUserFromHeader(authHeader);

        List<MealPlanEntry> entries = toEntries(req.entries());

        // ✅ Validierung + Recipe-Resolving passiert im Service
        MealPlan transientPlan = planService.buildTransientPlan(
                user,
                req.title(),
                req.weekStartMonday(),
                entries
        );

        byte[] pdf = planPdfService.createPlanPdf(transientPlan);

        String filename = (transientPlan.getTitle() == null ? "wochenplan" : transientPlan.getTitle())
                .replaceAll("[^a-zA-Z0-9\\-_ ]", "")
                .trim()
                .replace(" ", "_");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodeFilename(filename + ".pdf"))
                .body(pdf);
    }

    // ✅ Saubere Fehlermeldungen fürs Frontend (Toast zeigt exakt ex.getMessage())
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // ===== Helpers =====

    private List<MealPlanEntry> toEntries(List<PlanEntryDto> dtos) {
        if (dtos == null) return List.of();

        return dtos.stream().map(d -> {
            MealPlanEntry e = new MealPlanEntry();
            e.setDay(d.day());
            e.setSlot(d.slot());
            e.setServings(d.servings());

            // Wir setzen hier nur Recipe mit ID (Service löst es zu Entity auf)
            if (d.recipeId() != null) {
                com.example.rezeptapp.model.Recipe r = new com.example.rezeptapp.model.Recipe();
                r.setId(d.recipeId());
                e.setRecipe(r);
            } else {
                e.setRecipe(null);
            }
            return e;
        }).toList();
    }

    private UserAccount requireUserFromHeader(String authHeader) {
        try {
            String token = tokenFromHeader(authHeader);
            return authService.requireUser(token);
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "unauthorized");
        }
    }

    private String tokenFromHeader(String authHeader) {
        if (authHeader == null) throw new IllegalArgumentException("unauthorized");
        String prefix = "Bearer ";
        if (!authHeader.startsWith(prefix)) throw new IllegalArgumentException("unauthorized");
        return authHeader.substring(prefix.length()).trim();
    }

    private String encodeFilename(String name) {
        return java.net.URLEncoder.encode(name, StandardCharsets.UTF_8);
    }
}