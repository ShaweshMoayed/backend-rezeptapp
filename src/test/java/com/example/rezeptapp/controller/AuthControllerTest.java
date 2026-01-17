package com.example.rezeptapp.controller;

import com.example.rezeptapp.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @Autowired
    UserAccountRepository userRepo;

    private String randomUser() {
        return "user_" + System.nanoTime();
    }

    // ========= REGISTER =========

    @Test
    void register_ok() throws Exception {
        String u = randomUser();

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "pw123")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("registered"));
    }

    @Test
    void register_duplicateUsername_returns400() throws Exception {
        String u = randomUser();

        // first register
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "pw123")
                        )))
                .andExpect(status().isOk());

        // duplicate
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "pw123")
                        )))
                .andExpect(status().isBadRequest());
    }

    // ========= LOGIN =========

    @Test
    void login_ok_returnsToken() throws Exception {
        String u = randomUser();

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "pw123")
                        )))
                .andExpect(status().isOk());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "pw123")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        String u = randomUser();

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "pw123")
                        )))
                .andExpect(status().isOk());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "WRONG")
                        )))
                .andExpect(status().isUnauthorized());
    }

    // ========= ME =========

    @Test
    void me_withValidToken_returnsUser() throws Exception {
        String u = randomUser();

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "pw123")
                        )))
                .andExpect(status().isOk());

        String token = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "pw123")
                        )))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String extractedToken = om.readTree(token).get("token").asText();

        mvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + extractedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(u))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ========= LOGOUT =========

    @Test
    void logout_ok() throws Exception {
        String u = randomUser();

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "pw123")
                        )))
                .andExpect(status().isOk());

        String tokenJson = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(
                                new AuthController.AuthRequest(u, "pw123")
                        )))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = om.readTree(tokenJson).get("token").asText();

        mvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("logged_out"));

        // token should be invalid now
        mvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}