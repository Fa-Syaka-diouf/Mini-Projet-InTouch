package com.elfstack.toys.admin.domain;


import com.elfstack.toys.base.domain.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Entity
@Table(name= "jour_ferie", schema = "taskmanager_db" )

public class JourFerie{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jour_ferie_id")
    private Long id;

    @Getter
    @Setter
    @Column(name = "nom",nullable = false)
    private String nom;

    @Getter
    @Setter
    @Column(name = "date_debut",nullable = false)
    private LocalDate dateDebut;

    @Getter
    @Setter
    @Column(name = "date_fin")
    private LocalDate dateFin;


    @Setter
    @Column(name = "pays_code", nullable = false)
    private String paysCode;

    @Getter
    @Setter
    @Column(name = "pays", nullable = true)
    private String pays;

    @Getter
    @Setter
    @Nullable
    @Column(name = "description", length = 20000)
    private String description;

    @Getter
    @Setter
    @Nullable
    @Column(name = "type")
    private String type;

    @Getter
    @Setter
    @Column(name = "google_event_id", unique = true)
    private String googleEventId;


    public String getPaysCode() {
        return paysCode.toUpperCase();
    }

}
