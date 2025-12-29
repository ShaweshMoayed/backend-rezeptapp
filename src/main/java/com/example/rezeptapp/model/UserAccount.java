package com.example.rezeptapp.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_username", columnNames = "username")
)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60) // BCrypt-Hash ist ~60 Zeichen
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String username;

    public UserAccount() {}

    public UserAccount(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getPasswordHash() { return passwordHash; }
    public String getUsername() { return username; }

    public void setId(Long id) { this.id = id; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setUsername(String username) { this.username = username; }
}