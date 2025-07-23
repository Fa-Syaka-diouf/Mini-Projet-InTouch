package com.elfstack.toys.admin.domain;


import com.elfstack.toys.base.domain.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name= "jourFerie", schema = "taskmanager_db" )

public class JourFerie extends AbstractEntity<Long> {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jourFerie_Id")
    private Long id;

    @Getter
    @Setter
    @Column(name = "date")
    private LocalDate date;


    @Getter
    @Column(name = "paysCode")
    private String paysCode;

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
