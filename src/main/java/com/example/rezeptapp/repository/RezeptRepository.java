package com.example.rezeptapp.repository;

import com.example.rezeptapp.model.Rezept;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RezeptRepository extends JpaRepository<Rezept, Long> {
}