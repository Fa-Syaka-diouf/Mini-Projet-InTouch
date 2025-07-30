package com.elfstack.toys.admin.domain;


import com.elfstack.toys.base.domain.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name= "jour_ferie", schema = "taskmanager_db" )

public class JourFerie extends AbstractEntity<Long> {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jour_ferie_id")
    private Long id;

    @Getter
    @Setter
    @Column(name = "date_debut")
    private LocalDate date;


    @Getter
    @Column(name = "pays_code")
    private String pays_code;

    @Getter
    @Setter
    @Nullable
    @Column(name = "description")
    private String description;



    @Override
    public Long getId() {
        return id;
    }
}
