package com.elfstack.toys.taskmanagement.domain;

import com.elfstack.toys.base.domain.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAccessor;

@Entity
@Table(name = "task", schema = "taskmanager_db")
public class Task extends AbstractEntity<Long> {

    public static final int DESCRIPTION_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status = TaskStatus.NOUVEAU;

    @Lob
    @Column(name = "description")
    private String description;

    //    @NotNull(message = "Veuillez sélectionner un pays")
    @Column(name = "country", nullable = false)
    private String country;

    @Min(value = 1, message = "Le SLA doit être d'au moins 1 jour")
    @Column(name = "sla_days")
    private Integer slaDays;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private TaskPriority priority;

    @Column(name = "attachment_filename")
    private String attachmentFilename;

    @Column(name = "attachment_path")
    private String attachmentPath;

    @Column(name = "created_date")
    private LocalDate createdDate;

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
    @Column(name = "date_de_fin")
    @Nullable
    private LocalDate dateFin;

    @Override
    public @Nullable Long getId() {
        return id;
    }

    private String responsibleUsername;

    // Constructeurs
    public Task() {
        this.createdDate = LocalDate.now();
        this.status = TaskStatus.PENDING;
    }

    public Task(String title, String country) {
        this();
        this.title = title;
        this.country = country;
    }

    // Méthode pour calculer automatiquement la date limite
    public void calculateDueDate() {
        if (slaDays != null && createdDate != null) {
            this.dueDate = createdDate.plusDays(slaDays);
        }
    }

    // Getters et Setters
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getSlaDays() { return slaDays; }
    public void setSlaDays(Integer slaDays) {
        this.slaDays = slaDays;
        calculateDueDate(); // Recalcul automatique
    }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public String getAttachmentFilename() { return attachmentFilename; }
    public void setAttachmentFilename(String attachmentFilename) { this.attachmentFilename = attachmentFilename; }

    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
        calculateDueDate(); // Recalcul automatique
    }

    public void setResponsableFirstName(String responsableFirstName) {
    }

    public void setResponsableLastName(String responsableLastName) {
    }

    public Object getStatut() {
        return null;
    }

    public void setDateLimite(@Nullable LocalDate dateLimite) {
    }

    public void setCreationDate(Instant instant) {
    }

    public void setStatut(StatutEnum statut) {
    }

    public void setLibelle(String libelle) {
    }

    public Object getLibelle() {
        return null;
    }

    public Object getDateLimite() {
        return null;
    }

    public TemporalAccessor getCreationDate() {
        return null;
    }

    private String responsible;

    public String getResponsable() {
        return responsible;
    }

    public void setResponsable(String responsible) {
        this.responsible = responsible;
    }

}
