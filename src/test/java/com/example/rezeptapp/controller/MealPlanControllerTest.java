package com.example.rezeptapp.controller;

import com.example.rezeptapp.model.MealSlot;
import com.example.rezeptapp.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MealPlanControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserAccountRepository userRepo;

    // ========= Tests =========

    @Test
    void exportPdf_validPlan_returnsPdf200() throws Exception {
        String token = registerAndLoginReturnToken();

        LocalDate monday = currentMonday();
        MealPlanController.PlanRequest req = new MealPlanController.PlanRequest(
                "Mein Plan",
                monday,
                build7DaysEntries(monday, 1L) // DataSeeder: Rezept ID 1 sollte existieren
        );

        mvc.perform(post("/rezeptapp/plans/pdf")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().string(not(blankOrNullString())));
    }

    @Test
    void exportPdf_entriesEmpty_returns400() throws Exception {
        String token = registerAndLoginReturnToken();

        LocalDate monday = currentMonday();
        MealPlanController.PlanRequest req = new MealPlanController.PlanRequest(
                "Plan",
                monday,
                List.of()
        );

        mvc.perform(post("/rezeptapp/plans/pdf")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("entries darf nicht leer sein")));
    }

    @Test
    void exportPdf_missingAuth_returns401() throws Exception {
        LocalDate monday = currentMonday();
        MealPlanController.PlanRequest req = new MealPlanController.PlanRequest(
                "Plan",
                monday,
                build7DaysEntries(monday, 1L)
        );

        mvc.perform(post("/rezeptapp/plans/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exportPdf_pastWeek_returns400() throws Exception {
        String token = registerAndLoginReturnToken();

        LocalDate pastMonday = currentMonday().minusWeeks(1);
        MealPlanController.PlanRequest req = new MealPlanController.PlanRequest(
                "Plan",
                pastMonday,
                build7DaysEntries(pastMonday, 1L)
        );

        mvc.perform(post("/rezeptapp/plans/pdf")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Vergangenheit")));
    }

    @Test
    void exportPdf_dayWithoutAnyRecipe_returns400() throws Exception {
        String token = registerAndLoginReturnToken();

        LocalDate monday = currentMonday();

        // 7 Tage * 3 Slots, aber für EINEN Tag alle recipeId=null => muss 400 geben
        List<MealPlanController.PlanEntryDto> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);

            Long rid = (i == 3) ? null : 1L; // Donnerstag absichtlich leer

            entries.add(new MealPlanController.PlanEntryDto(day, MealSlot.BREAKFAST, rid, null));
            entries.add(new MealPlanController.PlanEntryDto(day, MealSlot.LUNCH, null, null));
            entries.add(new MealPlanController.PlanEntryDto(day, MealSlot.DINNER, null, null));
        }

        MealPlanController.PlanRequest req = new MealPlanController.PlanRequest(
                "Plan",
                monday,
                entries
        );

        mvc.perform(post("/rezeptapp/plans/pdf")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Bitte wähle für jeden Tag mindestens ein Rezept")));
    }

    // ========= Helpers =========

    private String registerAndLoginReturnToken() throws Exception {
        String u = "u_" + UUID.randomUUID();
        String p = "pw123";

        // register
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new AuthController.AuthRequest(u, p))))
                .andExpect(status().isOk());

        // login
        String json = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new AuthController.AuthRequest(u, p))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return om.readTree(json).get("token").asText();
    }

    private LocalDate currentMonday() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private List<MealPlanController.PlanEntryDto> build7DaysEntries(LocalDate monday, Long recipeIdEveryDay) {
        List<MealPlanController.PlanEntryDto> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);

            // pro Tag: Frühstück gesetzt, Lunch/Dinner leer
            entries.add(new MealPlanController.PlanEntryDto(day, MealSlot.BREAKFAST, recipeIdEveryDay, null));
            entries.add(new MealPlanController.PlanEntryDto(day, MealSlot.LUNCH, null, null));
            entries.add(new MealPlanController.PlanEntryDto(day, MealSlot.DINNER, null, null));
        }
        return entries;
    }
}