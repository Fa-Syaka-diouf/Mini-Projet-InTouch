package com.elfstack.toys.taskmanagement.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "jour_ferie", schema = "taskmanager_db")
public class Holiday {
    @Id
    @Column(name="jour_ferie_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jour_ferie_id;

    @Column(name="description")
    private String description;

    @Column(name = "date_debut")
    private LocalDate date_debut;

    @Column(name = "date_fin")
    private LocalDate date_fin;

    @Column(name="nom")
    private String nom;
    
    @Column(name="pays_code")
    private String pays_code;
    
    @Column(name="type")
    private String type;

    public LocalDate getEndDate() {
        return date_fin;
    }

    public LocalDate getStartDate() {
        return date_debut;
    }

    // Getters & Setters
}

