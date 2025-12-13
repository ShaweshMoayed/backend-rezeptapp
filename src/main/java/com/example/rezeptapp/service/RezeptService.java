package com.example.rezeptapp.service;

import com.example.rezeptapp.model.Rezept;
import com.example.rezeptapp.repository.RezeptRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RezeptService {

    private final RezeptRepository rezeptRepository;

    public RezeptService(RezeptRepository rezeptRepository) {
        this.rezeptRepository = rezeptRepository;
    }

    public List<Rezept> findAll() {
        return rezeptRepository.findAll();
    }

    public Rezept create(Rezept rezept) {
        return rezeptRepository.save(rezept);
    }
}