package com.example.rezeptapp.service;

import com.example.rezeptapp.model.UserAccount;
import com.example.rezeptapp.repository.UserAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    public boolean login(String username, String plainPassword) {
        return userRepo.findByUsername(username)
                .map(user -> encoder.matches(plainPassword, user.getPasswordHash()))
                .orElse(false);
    }
}