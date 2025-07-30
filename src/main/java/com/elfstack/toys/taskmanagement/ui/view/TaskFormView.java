package com.elfstack.toys.taskmanagement.ui.view;

import com.elfstack.toys.admin.ui.AdminLayout;
import com.elfstack.toys.admin.service.CalendarService;
import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.TaskStatus;
import com.elfstack.toys.taskmanagement.service.TaskService;
import com.elfstack.toys.usermanagement.service.KeycloakUserService;
import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
//import java.net.http.HttpClient;


@PageTitle("Créer une tâche")
@Component
@Route(value = "admin/task-management", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class TaskFormView extends VerticalLayout {

    private TaskForm form;

    @Autowired
    private TaskService taskService;
    private final CalendarService calendarService;
    private final KeycloakUserService keycloakUserService;

    private Grid<Task> grid = new Grid<>(Task.class);
    private Dialog formDialog = new Dialog();

    public TaskFormView(TaskService taskService, KeycloakUserService keycloakUserService, CalendarService calendarService)
    {
        this.form = new TaskForm(keycloakUserService, calendarService);
        this.taskService = taskService;
        this.keycloakUserService = keycloakUserService;
        this.calendarService = calendarService;

        // Pays supportés
//        countryBox.setItems("FR", "US", "DE", "SN");
        dueDateField.setReadOnly(true); // Empêche modification manuelle

        // Quand un pays est sélectionné.
//        countryBox.addValueChangeListener(event -> {
//            String code = event.getValue();
//        });

        addClassName("task-view");
        setSizeFull();

        configureGrid();
        configureForm();
        configureDialog();

        Button addTaskButton = new Button("Nouvelle tâche");
        addTaskButton.addClickListener(e -> openForm(new Task()));

        add(addTaskButton, grid);
        updateList();
    }

    private void configureGrid() {
        grid.addClassNames("task-grid");
        grid.setSizeFull();

        grid.addColumn(Task::getTitle).setHeader("Titre").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(task -> {
            TaskStatus status = task.getStatus();
            String label = status != null ? status.getDisplayName() : "Inconnu";
            String badge = status != null ? status.getBadgeVariant() : "contrast";

            Span badgeSpan = new Span(label);
            badgeSpan.getElement().getThemeList().add("badge " + badge);
            return badgeSpan;
        })).setHeader("Statut").setAutoWidth(true);

        grid.setColumns("title", "status", "country", "priority", "dueDate");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        // Personnaliser l'affichage du statut
        grid.getColumnByKey("status").setRenderer(
                new ComponentRenderer<>(task -> {
                    Span statusBadge = new Span(task.getStatus() != null ? task.getStatus().getDisplayName() : "Aucun statut");
                    statusBadge.getElement().getThemeList().add("badge " + task.getStatus().getBadgeVariant());
                    return statusBadge;
                })
        );

        // Personnaliser l'affichage de la priorité
        grid.getColumnByKey("priority").setRenderer(
                new ComponentRenderer<>(task -> {
                    if (task.getPriority() != null) {
                        Span priorityBadge = new Span(task.getPriority().getDisplayName());
                        String variant = task.getPriority().name().toLowerCase();
                        priorityBadge.getElement().getThemeList().add("badge " + variant);
                        return priorityBadge;
                    }
                    return new Span("-");
                })
        );

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                openForm(event.getValue());
            }
        });
        grid.asSingleSelect().addValueChangeListener(event -> {
            Task selectedTask = event.getValue();
            if (selectedTask != null) {
                form.setTask(selectedTask);  // Affiche les détails dans le formulaire
            } else {
                form.setTask(null);  // Réinitialise le formulaire
            }
        });

    }

    //    private ComboBox<String> countryBox = new ComboBox<>("Pays");
    private DatePicker dueDateField = new DatePicker("Date limite");


    private void configureForm() {
        form.setWidth("500px");

        // Configuration du callback pour l'upload
        form.setUploadCallback((inputStream, fileName, mimeType) -> {
            try {
                return taskService.saveUploadedFile(inputStream, fileName, mimeType);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la sauvegarde du fichier", e);
            }
        });

        form.getSaveButton().addClickListener(e -> saveTask());
        form.getCancelButton().addClickListener(e -> closeForm());
    }

    private void configureDialog() {
        formDialog.add(form);
        formDialog.setModal(true);
        formDialog.setDraggable(true);
        formDialog.setResizable(true);
    }

    private void openForm(Task task) {
        form.setTask(task);
        formDialog.setHeaderTitle(task.getId() == null ? "Nouvelle tâche" : "Modifier la tâche : " + task.getTitle());
        formDialog.open();
    }
    private Task task;
    private void closeForm() {
        formDialog.close();
//        form.setTask(null);
        if (task != null) {
            form.setTask(task);
        } else {
            Notification.show("Erreur : tâche introuvable").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        grid.asSingleSelect().clear();
    }

    private void saveTask() {
        System.out.println(">> task récupéré : " + task);
        try {
            if (form.isValid()) {
                Task task = form.getTask();

                if (task.getCountry() == null) {
                    Notification.show("Veuillez sélectionner un pays")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                task.setDueDate(dueDateField.getValue());
//                task.setCountry(countryBox.getValue());
                taskService.save(task);

                updateList();
                closeForm();

                Notification.show("Tâche sauvegardée avec succès !")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Veuillez corriger les erreurs dans le formulaire")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            Notification.show("Erreur lors de la sauvegarde : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateList() {
        grid.setItems(taskService.findAll());
    }
}
