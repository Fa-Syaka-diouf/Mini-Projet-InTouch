package com.elfstack.toys.taskmanagement.domain;

import com.elfstack.toys.base.domain.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "task", schema = "taskmanager_db")
public class Task extends AbstractEntity<Long> {

    public static final int DESCRIPTION_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @Setter
    @Getter
    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Setter
    @Getter
    @Column(name = "description", nullable = false)
    @Size(max = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Setter
    @Getter
    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Setter
    @Getter
    @Column(name = "statut", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatutEnum statut;

    @Setter
    @Getter
    @Column(name = "responsableId")
    private String responsableId;

    @Setter
    @Getter
    @Column(name = "responsableUsername")
    private String responsableUsername;


    @Setter
    @Getter
    @Column(name = "date_limite")
    @Nullable
    private LocalDate dateLimite;

    @Setter
    @Getter
    @Column(name = "date_de_fin")
    @Nullable
    private LocalDate dateFin;

    @Override
    public @Nullable Long getId() {
        return id;
    }

}