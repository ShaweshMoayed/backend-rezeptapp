package com.example.rezeptapp.service;

import com.example.rezeptapp.model.UserAccount;
import com.example.rezeptapp.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired AuthService authService;
    @Autowired UserAccountRepository userRepo;

    @BeforeEach
    void cleanDb() {
        userRepo.deleteAll();
    }

    @Test
    void register_success_createsUser() {
        authService.register("kaka", "secret");

        UserAccount u = userRepo.findByUsername("kaka").orElseThrow();
        assertEquals("kaka", u.getUsername());
        assertNotNull(u.getPasswordHash());
        assertFalse(u.getPasswordHash().isBlank());
    }

    @Test
    void register_usernameEmpty_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("   ", "secret"));
        assertTrue(ex.getMessage().toLowerCase().contains("benutzername"));
    }

    @Test
    void register_passwordEmpty_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("kaka", ""));
        assertTrue(ex.getMessage().toLowerCase().contains("passwort"));
    }

    @Test
    void register_duplicateUsername_throws() {
        authService.register("kaka", "secret");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register("kaka", "secret2"));
        assertTrue(ex.getMessage().toLowerCase().contains("bereits"));
    }

    @Test
    void login_success_setsToken() {
        authService.register("kaka", "secret");

        String token = authService.loginAndCreateToken("kaka", "secret");
        assertNotNull(token);
        assertFalse(token.isBlank());

        UserAccount u = userRepo.findByUsername("kaka").orElseThrow();
        assertEquals(token, u.getAuthToken());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        authService.register("kaka", "secret");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.loginAndCreateToken("kaka", "WRONG"));
        assertEquals("invalid credentials", ex.getMessage());
    }

    @Test
    void login_unknownUser_throwsInvalidCredentials() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.loginAndCreateToken("unknown", "secret"));
        assertEquals("invalid credentials", ex.getMessage());
    }

    @Test
    void logout_clearsToken_ifExists() {
        authService.register("kaka", "secret");
        String token = authService.loginAndCreateToken("kaka", "secret");

        authService.logout(token);

        UserAccount u = userRepo.findByUsername("kaka").orElseThrow();
        assertNull(u.getAuthToken());
    }

    @Test
    void requireUser_validToken_returnsUserWithFavoritesLoaded() {
        authService.register("kaka", "secret");
        String token = authService.loginAndCreateToken("kaka", "secret");

        UserAccount u = authService.requireUser(token);

        assertNotNull(u);
        assertEquals("kaka", u.getUsername());
        assertNotNull(u.getFavorites()); // should be loaded via EntityGraph
    }

    @Test
    void requireUser_invalidToken_throwsUnauthorized() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.requireUser("does-not-exist"));
        assertEquals("unauthorized", ex.getMessage());
    }
}