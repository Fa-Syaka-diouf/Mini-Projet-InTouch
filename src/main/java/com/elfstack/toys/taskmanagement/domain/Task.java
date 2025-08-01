package com.elfstack.toys.taskmanagement.domain;

import com.elfstack.toys.base.domain.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Entity
@Table(name = "task", schema = "taskmanager_db")
public class Task extends AbstractEntity<Long> {

    public static final int DESCRIPTION_MAX_LENGTH = 255;

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private TaskStatus statut = TaskStatus.NOUVEAU;

    @Setter
    @Lob
    @Column(name = "description")
    private String description;

    @Setter
    @Column(name = "country", nullable = false)
    private String country;

    @Min(1)
    @Column(name = "sla_days")
    private Integer slaDays;

    @Setter
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private TaskPriority priority;

    @Setter
    @Column(name = "attachment_filename")
    private String attachmentFilename;

    @Setter
    @Column(name = "attachment_path")
    private String attachmentPath;

    @Column(name = "created_date")
    private LocalDate createdDate = LocalDate.now();

    @Setter
    @Column(name = "responsable_id")
    private String responsableId;

    @Setter
    @Column(name = "responsable_username")
    private String responsableUsername;

    @Setter
    @Column(name = "responsable_lastname")
    private String responsableLastname;

    @Setter
    @Column(name = "responsable_firstname")
    private String responsableFirstname;

    @Setter
    @Column(name = "date_de_fin")
    private LocalDate dateFin;

    // Getters & Setters

    public void setSlaDays(Integer slaDays) {
        this.slaDays = slaDays;
        calculateDueDate();
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
        calculateDueDate();
    }

    public void calculateDueDate() {
        this.dueDate = createdDate.plusDays(slaDays);
    }
}
