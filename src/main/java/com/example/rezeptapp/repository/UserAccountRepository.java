package com.example.rezeptapp.repository;

import com.example.rezeptapp.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    boolean existsByUsername(String username);

    Optional<UserAccount> findByAuthToken(String authToken);
}