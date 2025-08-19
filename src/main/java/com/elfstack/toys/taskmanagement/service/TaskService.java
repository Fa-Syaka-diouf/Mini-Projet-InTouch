package com.elfstack.toys.taskmanagement.service;

import com.elfstack.toys.taskmanagement.domain.StatutEnum;
import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.TaskPriority;
import com.elfstack.toys.taskmanagement.domain.TaskRepository;
import com.elfstack.toys.usermanagement.service.KeycloakUserService;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@PreAuthorize("isAuthenticated()")
public class TaskService {

    private final TaskRepository taskRepository;
    private final Clock clock;
    private final KeycloakUserService keycloakUserService;

    TaskService(TaskRepository taskRepository, Clock clock, KeycloakUserService keycloakUserService) {
        this.taskRepository = taskRepository;
        this.clock = clock;
        this.keycloakUserService = keycloakUserService;
    }

    @Transactional
    public Task createTask(String libelle, String description, @Nullable LocalDate dateLimite,
                           @Nullable String responsableId, String responsableFullname,
                           @Nullable String paysDestinataire, @Nullable Long slaDays,
                           @Nullable TaskPriority priority, StatutEnum statut) {
        if ("fail".equals(description)) {
            throw new RuntimeException("This is for testing the error handler");
        }

        var task = new Task();
        task.setLibelle(libelle);
        task.setDescription(description);
        task.setCreationDate(clock.instant());
        task.setResponsableId(responsableId);
        task.setResponsableFullname(responsableFullname);
        task.setPaysDestinataire(paysDestinataire);
        task.setSlaDays(slaDays);
        task.setPriority(priority != null ? priority : TaskPriority.MOYENNE);
        task.setDateLimite(dateLimite);
        task.setStatut(statut != null ? statut : StatutEnum.A_FAIRE);

        return taskRepository.saveAndFlush(task);
    }

    @Transactional
    public Task save(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("La tâche ne peut pas être null");
        }
        if (task.getId() == null && task.getCreationDate() == null) {
            task.setCreationDate(clock.instant());
            task.setResponsableId(keycloakUserService.getUserIdByUserName(task.getResponsableUsername()));
            task.setResponsableFullname(keycloakUserService.getFullNameByUserName(task.getResponsableUsername()));

        }

        return taskRepository.saveAndFlush(task);
    }

    @Transactional
    public Task update(Task task) {
        if (task == null || task.getId() == null) {
            throw new IllegalArgumentException("La tâche et son ID ne peuvent pas être null pour une mise à jour");
        }

        Optional<Task> existingTask = taskRepository.findById(task.getId());
        if (existingTask.isEmpty()) {
            throw new IllegalArgumentException("Tâche introuvable avec l'ID : " + task.getId());
        }

        // Conserver la date de création originale
        task.setCreationDate(existingTask.get().getCreationDate());

        return taskRepository.saveAndFlush(task);
    }

    @Transactional
    public void delete(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("L'ID de la tâche ne peut pas être null");
        }
        taskRepository.deleteById(taskId);
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Task> list(Pageable pageable) {
        return taskRepository.findAllBy(pageable).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> findByResponsableFullname(String fullname) {
        return taskRepository.findByResponsableFullname(fullname);
    }

    @Transactional(readOnly = true)

    public List<Task> getTasksEnRetard() {
        return taskRepository.findTasksEnRetard(PageRequest.of(0, 5));
    }

    LocalDate today = LocalDate.now();
    LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

    Pageable topFive = PageRequest.of(0, 5);
    @Transactional(readOnly = true)
    public List<Task> getTasksAVenir() {
        return taskRepository.findNextTasks(today, endOfWeek, topFive);
    }
    @Transactional(readOnly = true)
    public List<Task> findByStatut(StatutEnum statut) {
        return taskRepository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<Task> findByPriority(TaskPriority priority) {
        return taskRepository.findByPriority(priority);
    }

    @Transactional
    public void markAsCompleted(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatut(StatutEnum.TERMINER);
            task.setDateFin(LocalDateTime.now(clock));
            taskRepository.saveAndFlush(task);
        }
    }

    public String saveUploadedFile(InputStream inputStream, String fileName, String mimeType) {
        // Implémentation de la sauvegarde de fichier
        return fileName;
    }
}