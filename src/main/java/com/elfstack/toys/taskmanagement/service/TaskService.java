package com.elfstack.toys.taskmanagement.service;

import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.TaskRepository;
import com.elfstack.toys.taskmanagement.domain.TaskStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@PreAuthorize("isAuthenticated()")
public class TaskService {

    private final TaskRepository taskRepository;
    private final Clock clock;

    public TaskService(TaskRepository taskRepository, Clock clock) {
        this.taskRepository = taskRepository;
        this.clock = clock;
    }

    /**
     * Crée une nouvelle tâche et la sauvegarde en base.
     */
    @Transactional
    public void createTask(String title, String description, @Nullable LocalDate dueDate,
                           @Nullable String responsableId, @Nullable LocalDate endDate,
                           String responsableFirstName, String responsableLastName, TaskStatus status) {

        if ("fail".equalsIgnoreCase(description)) {
            throw new RuntimeException("This is for testing the error handler");
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCreatedDate(LocalDate.now(clock));
        task.setDueDate(dueDate);
        task.setDateFin(endDate);
        task.setStatut(status);

        if (responsableId != null) {
            task.setResponsableId(responsableId);
        }

        String fullName = (responsableFirstName != null ? responsableFirstName : "") +
                " " +
                (responsableLastName != null ? responsableLastName : "");
        task.setResponsableUsername(fullName.trim());

        // Tu peux aussi stocker l'username complet si tu veux
        task.setResponsableUsername(responsableLastName); // ou autre logique ici

        taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> list(Pageable pageable) {
        return taskRepository.findAllBy(pageable).toList();
    }

    /**
     * Sauvegarde ou met à jour une tâche.
     */
    @Transactional
    public void save(Task task) {
        taskRepository.save(task);
    }

    /**
     * Méthode inutile actuellement – peut être supprimée ou convertie pour retourner toutes les tâches.
     */
    public Task findAll() {
        return null; // à corriger ou supprimer
    }

    /**
     * Sauvegarde d'un fichier associé à une tâche (non implémentée ici).
     */
    public String saveUploadedFile(InputStream inputStream, String fileName, String mimeType) {
        // à implémenter si nécessaire
        return fileName;
    }
}
