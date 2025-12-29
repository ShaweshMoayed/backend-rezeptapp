package com.example.rezeptapp.controller;

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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        authService.register(req.username(), req.password());
        return ResponseEntity.ok("registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        boolean ok = authService.login(req.username(), req.password());
        if (!ok) return ResponseEntity.status(401).body("invalid credentials");
        return ResponseEntity.ok("login ok");
    }
}