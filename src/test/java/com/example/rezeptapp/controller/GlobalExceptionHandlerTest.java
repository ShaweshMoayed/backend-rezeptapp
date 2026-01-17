package com.example.rezeptapp.controller;

import com.example.rezeptapp.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    @RestController
    static class DummyController {
        @GetMapping("/boom")
        public String boom() {
            throw new RuntimeException("kaputt");
        }

        @GetMapping("/unauth")
        public String unauth() {
            throw new IllegalArgumentException("unauthorized");
        }
    }

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new DummyController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void illegalArgument_unauthorized_mapsTo401() throws Exception {
        mvc.perform(get("/unauth"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error", containsString("Unauthorized")))
                .andExpect(jsonPath("$.message").value("unauthorized"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void genericException_mapsTo500() throws Exception {
        mvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error", containsString("Internal Server Error")))
                .andExpect(jsonPath("$.message").value("internal server error"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}