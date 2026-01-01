package com.example.rezeptapp.service;

import com.example.rezeptapp.model.UserAccount;
import com.example.rezeptapp.repository.UserAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserAccountRepository userRepo;
    private final BCryptPasswordEncoder encoder;

    public AuthService(UserAccountRepository userRepo, BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    public void register(String username, String plainPassword) {
        String u = username == null ? "" : username.trim();
        String p = plainPassword == null ? "" : plainPassword;

        if (u.isEmpty()) {
            throw new IllegalArgumentException("Benutzername darf nicht leer sein.");
        }
        if (p.isEmpty()) {
            throw new IllegalArgumentException("Passwort darf nicht leer sein.");
        }
        if (userRepo.existsByUsername(u)) {
            throw new IllegalArgumentException("Benutzername ist bereits vergeben.");
        }

        String hash = encoder.encode(p);
        userRepo.save(new UserAccount(u, hash));
    }

    public String loginAndCreateToken(String username, String plainPassword) {
        String u = username == null ? "" : username.trim();
        String p = plainPassword == null ? "" : plainPassword;

        UserAccount user = userRepo.findByUsername(u)
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));

        if (!encoder.matches(p, user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        user.setAuthToken(token);
        userRepo.save(user);

        return token;
    }

    public void logout(String token) {
        userRepo.findByAuthToken(token).ifPresent(u -> {
            u.setAuthToken(null);
            userRepo.save(u);
        });
    }

    public UserAccount requireUser(String token) {
        return userRepo.findWithFavoritesByAuthToken(token)
                .orElseThrow(() -> new IllegalArgumentException("unauthorized"));
    }
}