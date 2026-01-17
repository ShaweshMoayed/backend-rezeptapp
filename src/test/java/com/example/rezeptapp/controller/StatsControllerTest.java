package com.example.rezeptapp.controller;

import com.example.rezeptapp.config.GlobalExceptionHandler;
import com.example.rezeptapp.service.StatsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StatsControllerTest {

    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();

    @Test
    void stats_ok_returns200() throws Exception {
        StatsService service = mock(StatsService.class);

        StatsService.StatsResponse resp = new StatsService.StatsResponse(
                List.of(),
                new StatsService.Macro(100, 1, 2, 3),
                new StatsService.Macro(100, 1, 2, 3)
        );

        when(service.buildStats(any())).thenReturn(resp);

        StatsController controller = new StatsController(service);

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        StatsService.StatsRequest req = new StatsService.StatsRequest(List.of(1L, 2L, 3L));

        mvc.perform(post("/rezeptapp/stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total.caloriesKcal").value(100.0));
    }

    @Test
    void stats_emptyIds_returns400WithMessage() throws Exception {
        StatsService service = mock(StatsService.class);
        when(service.buildStats(any())).thenThrow(new IllegalArgumentException("recipeIds darf nicht leer sein"));

        StatsController controller = new StatsController(service);

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        StatsService.StatsRequest req = new StatsService.StatsRequest(List.of());

        mvc.perform(post("/rezeptapp/stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("recipeIds darf nicht leer sein"));
    }

    @Test
    void stats_missingId_returns404WithMessage() throws Exception {
        StatsService service = mock(StatsService.class);
        when(service.buildStats(any())).thenThrow(new IllegalArgumentException("Recipe nicht gefunden: [9999]"));

        StatsController controller = new StatsController(service);

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        StatsService.StatsRequest req = new StatsService.StatsRequest(List.of(9999L));

        mvc.perform(post("/rezeptapp/stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Recipe nicht gefunden")));
    }
}