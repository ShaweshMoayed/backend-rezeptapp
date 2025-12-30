package com.example.rezeptapp.controller;

import com.example.rezeptapp.model.UserAccount;
import com.example.rezeptapp.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record AuthRequest(String username, String password) {}
    public record LoginResponse(String token) {}
    public record MeResponse(Long id, String username) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        authService.register(req.username(), req.password());
        return ResponseEntity.ok("registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        String token = authService.loginAndCreateToken(req.username(), req.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = tokenFromHeader(authHeader);
        authService.logout(token);
        return ResponseEntity.ok("logged out");
    }

    // ✅ GET /auth/me
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        String token = tokenFromHeader(authHeader);
        UserAccount u = authService.requireUser(token);
        return ResponseEntity.ok(new MeResponse(u.getId(), u.getUsername()));
    }

    private String tokenFromHeader(String authHeader) {
        if (authHeader == null) throw new IllegalArgumentException("unauthorized");
        String prefix = "Bearer ";
        if (!authHeader.startsWith(prefix)) throw new IllegalArgumentException("unauthorized");

        String token = authHeader.substring(prefix.length()).trim();
        if (token.isEmpty()) throw new IllegalArgumentException("unauthorized");

        return token;
    }
}