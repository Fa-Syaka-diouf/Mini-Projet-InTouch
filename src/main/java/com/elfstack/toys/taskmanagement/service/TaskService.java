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

    TaskService(TaskRepository taskRepository, Clock clock) {
        this.taskRepository = taskRepository;
        this.clock = clock;
    }

    @Transactional
    public void createTask(String libelle, String description, @Nullable LocalDate dateLimite,
                           @Nullable String responsableId, @Nullable LocalDate dateFin, String responsableLastname, String responsableFirstname, TaskStatus statut ) {
        if ("fail".equals(description)) {
            throw new RuntimeException("This is for testing the error handler");
        }

        var task = new Task();
        task.setTitle(libelle);
        task.setDescription(description);
        task.setCreatedDate(LocalDate.from(clock.instant()));
        task.setResponsableUsername(responsableLastname);
        assert dateLimite != null;
        task.setDueDate(dateLimite);
        assert responsableId != null;
        task.setResponsableId(responsableId);
        assert dateFin != null;
        task.setDateFin(dateFin);
        task.setStatut(statut);

        taskRepository.saveAndFlush(task);
    }

    @Transactional(readOnly = true)
    public List<Task> list(Pageable pageable) {
        return taskRepository.findAllBy(pageable).toList();
    }

    public String saveUploadedFile(InputStream inputStream, String fileName, String mimeType) {
        return fileName;
    }

    public void save(Task task) {
    }

    public Task findAll() {
        return null;
    }
}