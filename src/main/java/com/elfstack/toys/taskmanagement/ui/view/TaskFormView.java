package com.elfstack.toys.taskmanagement.ui.view;

import com.elfstack.toys.admin.service.HolidaySyncService;
import com.elfstack.toys.admin.ui.AdminLayout;
import com.elfstack.toys.admin.service.CalendarService;
import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.StatutEnum;
import com.elfstack.toys.taskmanagement.domain.TaskPriority;
import com.elfstack.toys.taskmanagement.service.TaskService;
import com.elfstack.toys.usermanagement.service.KeycloakUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@PageTitle("Gestion des tâches")
@Component
@Route(value = "admin/task-management", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class TaskFormView extends VerticalLayout {

    private final TaskService taskService;
    private final CalendarService calendarService;
    private final KeycloakUserService keycloakUserService;
    private final HolidaySyncService holidaySyncService;

    // Composants de la grille
    private Grid<Task> grid = new Grid<>(Task.class, false);
    private Button addTaskButton = new Button("Nouvelle tâche");

    // Composants du formulaire
    private Dialog formDialog = new Dialog();
    private FormLayout formLayout = new FormLayout();
    private TextField libelle = new TextField("Libellé (*)");
    private ComboBox<StatutEnum> statut = new ComboBox<>("Statut (*)");
    private TextArea description = new TextArea("Description (*)");
    private IntegerField slaDays = new IntegerField("SLA (jours) *");
    private ComboBox<String> paysDestinataire = new ComboBox<>("Pays destinataire (*)");
    private DatePicker dateLimite = new DatePicker("Date limite calculée");
    private ComboBox<TaskPriority> priority = new ComboBox<>("Priorité (*)");
    private ComboBox<String> responsableUsername = new ComboBox<>("Responsable (*)");
    private Button saveButton = new Button("Enregistrer");
    private Button cancelButton = new Button("Annuler");
    private Button deleteButton = new Button("Supprimer");

    private final Binder<Task> binder = new BeanValidationBinder<>(Task.class);
    private Task currentTask;

    public TaskFormView(TaskService taskService,
                        KeycloakUserService keycloakUserService,
                        CalendarService calendarService,
                        HolidaySyncService holidaySyncService) {
        this.taskService = taskService;
        this.keycloakUserService = keycloakUserService;
        this.calendarService = calendarService;
        this.holidaySyncService = holidaySyncService;

        addClassName("task-management-view");
        setSizeFull();

        configureGrid();
        configureForm();
        configureDialog();

        add(addTaskButton, grid);
        updateList();
        loadInitialData();
    }

    private void configureGrid() {
        grid.addClassNames("task-grid");
        grid.setSizeFull();

        grid.addColumn(Task::getLibelle).setHeader("Libellé").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(task -> {
            StatutEnum status = task.getStatut();
            String label = status != null ? status.name() : "Inconnu";
            String badgeVariant = getStatusBadgeVariant(status);

            Span badgeSpan = new Span(label);
            badgeSpan.getElement().getThemeList().add("badge " + badgeVariant);
            return badgeSpan;
        })).setHeader("Statut").setAutoWidth(true);

        grid.addColumn(Task::getResponsableUsername).setHeader("Responsable").setAutoWidth(true);
        grid.addColumn(Task::getPaysDestinataire).setHeader("Pays").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(task -> {
            TaskPriority taskPriority = task.getPriority();
            if (taskPriority != null) {
                Span priorityBadge = new Span(taskPriority.name());
                String variant = getPriorityBadgeVariant(taskPriority);
                priorityBadge.getElement().getThemeList().add("badge " + variant);
                return priorityBadge;
            }
            return new Span("-");
        })).setHeader("Priorité").setAutoWidth(true);

        grid.addColumn(Task::getDateLimite).setHeader("Date limite").setAutoWidth(true);

        // Gestion de la sélection
        grid.asSingleSelect().addValueChangeListener(event -> {
            openForm(event.getValue());
        });

        // Configuration du bouton d'ajout
        addTaskButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTaskButton.addClickListener(e -> openForm(new Task()));
    }

    private void configureForm() {
        configureFormFields();
        configureBinder();
        configureFormButtons();
        configureFormLayout();
    }

    private void configureFormFields() {
        // Configuration du libellé
        libelle.setPlaceholder("Titre de la tâche");
        libelle.setRequired(true);

        // Configuration du statut
        statut.setItems(StatutEnum.values());
        statut.setItemLabelGenerator(StatutEnum::name);
        statut.setPlaceholder("Sélectionner un statut");
        statut.setValue(StatutEnum.A_FAIRE);
        statut.setRequired(true);

        // Configuration de la description
        description.setPlaceholder("Description détaillée de la tâche");
        description.setHeight("120px");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setRequired(true);

        // Configuration du SLA
        slaDays.setMin(1);
        slaDays.setStep(1);
        slaDays.setPlaceholder("Nombre de jours pour le SLA");
        slaDays.setRequiredIndicatorVisible(true);

        // Configuration du pays
        paysDestinataire.setPlaceholder("Sélectionner un pays");
        paysDestinataire.setRequired(true);

        // Configuration de la date limite
        dateLimite.setPlaceholder("jj/mm/aaaa");
        dateLimite.setLocale(Locale.FRANCE);
        dateLimite.setReadOnly(true);

        // Configuration de la priorité
        priority.setItems(TaskPriority.values());
        priority.setItemLabelGenerator(TaskPriority::name);
        priority.setPlaceholder("Sélectionner une priorité");
        priority.setValue(TaskPriority.NORMALE);
        priority.setRequired(true);

        // Configuration du responsable
        responsableUsername.setPlaceholder("Sélectionner un responsable");
        responsableUsername.setRequired(true);

        // Listeners pour le calcul automatique de la date limite
        paysDestinataire.addValueChangeListener(e -> updateDueDate());
        slaDays.addValueChangeListener(e -> updateDueDate());
    }

    private void configureBinder() {
        binder.forField(libelle)
                .asRequired("Le libellé est obligatoire")
                .withValidator(t -> t != null && t.trim().length() >= 3, "Le libellé doit contenir au moins 3 caractères")
                .bind(Task::getLibelle, Task::setLibelle);

        binder.forField(statut)
                .asRequired("Le statut est obligatoire")
                .bind(Task::getStatut, Task::setStatut);

        binder.forField(description)
                .asRequired("La description est obligatoire")
                .withValidator(d -> d != null && !d.trim().isEmpty(), "La description ne peut pas être vide")
                .bind(Task::getDescription, Task::setDescription);

        binder.forField(paysDestinataire)
                .asRequired("Le pays est obligatoire")
                .bind(Task::getPaysDestinataire, Task::setPaysDestinataire);

        binder.forField(dateLimite)
                .bind(Task::getDateLimite, Task::setDateLimite);

        binder.forField(priority)
                .asRequired("La priorité est obligatoire")
                .bind(Task::getPriority, Task::setPriority);

        binder.forField(slaDays)
                .asRequired("Le SLA est obligatoire")
                .withValidator(val -> val != null && val >= 1, "Le SLA doit être au moins 1")
                .withConverter(
                        integer -> integer != null ? Long.valueOf(integer) : null,
                        longValue -> longValue != null ? Math.toIntExact(longValue) : null
                )
                .bind(Task::getSlaDays, Task::setSlaDays);

        binder.forField(responsableUsername)
                .asRequired("Responsable obligatoire")
                .bind(Task::getResponsableUsername, Task::setResponsableUsername);
    }

    private void configureFormButtons() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        saveButton.getStyle().set("margin-right", "var(--lumo-space-s)");
        deleteButton.getStyle().set("margin-left", "auto");

        saveButton.addClickListener(e -> saveTask());
        cancelButton.addClickListener(e -> closeForm());
        deleteButton.addClickListener(e -> deleteTask());
    }

    private void configureFormLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton, deleteButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.START);
        buttonLayout.setWidthFull();

        formLayout.add(
                libelle,
                statut,
                description,
                responsableUsername,
                paysDestinataire,
                slaDays,
                dateLimite,
                priority,
                buttonLayout
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        formLayout.setColspan(description, 2);
        formLayout.setColspan(buttonLayout, 2);
    }

    private void configureDialog() {
        formDialog.add(formLayout);
        formDialog.setModal(true);
        formDialog.setDraggable(true);
        formDialog.setResizable(true);
        formDialog.setWidth("800px");
        formDialog.setHeight("600px");
    }

    private void loadInitialData() {
        loadAvailableCountries();
        loadResponsibleUsers();
    }

    private void loadAvailableCountries() {
        try {
            List<String> countries = holidaySyncService.getAllCountries();
            if (countries != null && !countries.isEmpty()) {
                paysDestinataire.setItems(countries);
            } else {
                paysDestinataire.setItems("FR", "UK", "US", "DE", "ES", "IT");
                paysDestinataire.setValue("FR");
                Notification.show("Aucun pays configuré, utilisation des pays par défaut")
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        } catch (Exception e) {
            paysDestinataire.setItems("FR", "UK", "US", "DE", "ES", "IT");
            paysDestinataire.setValue("FR");
            Notification.show("Erreur lors du chargement des pays : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadResponsibleUsers() {
        try {
            List<String> usernames = keycloakUserService.getAllUsernames();
            responsableUsername.setItems(usernames);
        } catch (Exception e) {
            responsableUsername.setItems();
            Notification.show("Erreur lors du chargement des utilisateurs")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openForm(Task task) {
        this.currentTask = task;

        if (task.getId() == null) {
            // Nouvelle tâche
            task.setCreationDate(Instant.now());
            task.setStatut(StatutEnum.A_FAIRE);
            task.setPriority(TaskPriority.NORMALE);
            deleteButton.setVisible(false);
        } else {
            // Tâche existante
            deleteButton.setVisible(true);
        }

        binder.setBean(task);

        // Calculer la date limite après avoir défini la tâche
        updateDueDate();

        formDialog.setHeaderTitle(task.getId() == null ? "Nouvelle tâche" : "Modifier la tâche : " + task.getLibelle());
        formDialog.open();
    }

    private void closeForm() {
        formDialog.close();
        grid.asSingleSelect().clear();
        currentTask = null;
        binder.setBean(null);
    }

    private void saveTask() {
        try {
            if (binder.writeBeanIfValid(currentTask)) {
                Task savedTask;

                if (currentTask.getId() == null) {
                    // Nouvelle tâche - utiliser createTask ou save
                    savedTask = taskService.save(currentTask);
                } else {
                    // Mise à jour d'une tâche existante
                    savedTask = taskService.update(currentTask);
                }

                updateList();
                closeForm();

                String message = currentTask.getId() == null ?
                        "Tâche créée avec succès !" : "Tâche mise à jour avec succès !";
                Notification.show(message)
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

    private void deleteTask() {
        if (currentTask != null && currentTask.getId() != null) {
            try {
                taskService.delete(currentTask.getId());
                updateList();
                closeForm();
                Notification.show("Tâche supprimée avec succès !")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception e) {
                Notification.show("Erreur lors de la suppression : " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }

    private void updateDueDate() {
        try {
            if (slaDays.getValue() != null && paysDestinataire.getValue() != null && currentTask != null) {
                // Utiliser la date de création de la tâche si disponible
                LocalDate startDate;
                if (currentTask.getCreationDate() != null) {
                    startDate = currentTask.getCreationDate().atZone(ZoneId.systemDefault()).toLocalDate();
                } else {
                    startDate = LocalDate.now();
                }

                String countryCode = holidaySyncService.countryCodeSetup(paysDestinataire.getValue());

                // Calculer la date limite
                LocalDate computedDueDate = calendarService.calculateDueDate(
                        startDate,
                        slaDays.getValue(),
                        countryCode
                );

                dateLimite.setValue(computedDueDate);

                // Mettre à jour la tâche
                if (currentTask != null) {
                    currentTask.setDateLimite(computedDueDate);
                }
            } else {
                // Nettoyer la date si les paramètres sont incomplets
                dateLimite.clear();
                if (currentTask != null) {
                    currentTask.setDateLimite(null);
                }
            }
        } catch (Exception e) {
            // Gestion d'erreur lors du calcul
            Notification.show("Erreur lors du calcul de la date limite : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            dateLimite.clear();
            if (currentTask != null) {
                currentTask.setDateLimite(null);
            }
        }
    }

    private void updateList() {
        try {
            List<Task> tasks = taskService.findAll();
            grid.setItems(tasks);
        } catch (Exception e) {
            Notification.show("Erreur lors du chargement des tâches : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String getStatusBadgeVariant(StatutEnum status) {
        if (status == null) return "contrast";

        return switch (status) {
            case A_FAIRE -> "contrast";
            case EN_COURS -> "primary";
            case TERMINER -> "success";
            case SUSPENDU -> "error";
            default -> "contrast";
        };
    }

    private String getPriorityBadgeVariant(TaskPriority priority) {
        if (priority == null) return "contrast";

        return switch (priority) {
            case FAIBLE -> "success";
            case NORMALE -> "contrast";
            case ELEVEE -> "primary";
            case CRITIQUE -> "error";
            default -> "contrast";
        };
    }
}