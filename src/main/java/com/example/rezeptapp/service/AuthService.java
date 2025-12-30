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
        if (username == null || username.isBlank() || username.length() < 3) {
            throw new IllegalArgumentException("Username muss mind. 3 Zeichen haben.");
        }
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Passwort muss mind. 6 Zeichen haben.");
        }
        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username ist bereits vergeben.");
        }

        String hash = encoder.encode(plainPassword);
        userRepo.save(new UserAccount(username, hash));
    }

    public String loginAndCreateToken(String username, String plainPassword) {
        UserAccount user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));

        if (!encoder.matches(plainPassword, user.getPasswordHash())) {
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