package com.elfstack.toys.taskmanagement.domain;

import com.elfstack.toys.base.domain.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;


@Entity
@Table(name = "task", schema = "taskmanager_db")
public class Task extends AbstractEntity<Long> {

    public static final int DESCRIPTION_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status = TaskStatus.NOUVEAU;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "country", nullable = false)
    private String country;

    @Min(1)
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
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "responsable_id")
    private String responsableId;

    @Column(name = "responsable_username")
    private String responsableUsername;

    @Column(name = "responsable")
    private String responsable;

    @Column(name = "date_de_fin")
    private LocalDate dateFin;

    // Getters & Setters

    public Long getId() { return id; }
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
        calculateDueDate();
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
        calculateDueDate();
    }

    public String getResponsableId() { return responsableId; }
    public void setResponsableId(String responsableId) { this.responsableId = responsableId; }

    public String getResponsableUsername() { return responsableUsername; }
    public void setResponsableUsername(String responsableUsername) { this.responsableUsername = responsableUsername; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public void calculateDueDate() {
        if (slaDays != null && createdDate != null) {
            this.dueDate = createdDate.plusDays(slaDays);
        }
    }
}
