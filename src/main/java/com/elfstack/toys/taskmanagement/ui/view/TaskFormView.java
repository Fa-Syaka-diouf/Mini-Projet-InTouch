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
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@PageTitle("Gestion des tâches")
@Route(value = "admin/task-management", layout = AdminLayout.class)
@UIScope
@RolesAllowed("ADMIN")
public class TaskFormView extends VerticalLayout {

    private final TaskService taskService;
    private final CalendarService calendarService;
    private final KeycloakUserService keycloakUserService;
    private final HolidaySyncService holidaySyncService;

    private final Grid<Task> grid = new Grid<>(Task.class, false);
    private final Button addTaskButton = new Button("Nouvelle tâche");
    private TextField searchField;
    private GridListDataView<Task> dataView;
    private Editor<Task> editor;

    private final Dialog formDialog = new Dialog();
    private final FormLayout formLayout = new FormLayout();
    private final TextField libelle = new TextField("Libellé (*)");
    private final ComboBox<StatutEnum> statut = new ComboBox<>("Statut (*)");
    private final TextArea description = new TextArea("Description (*)");
    private final IntegerField slaDays = new IntegerField("SLA (jours) *");
    private final ComboBox<String> paysDestinataire = new ComboBox<>("Pays destinataire (*)");
    private final DatePicker dateLimite = new DatePicker("Date limite calculée");
    private final ComboBox<TaskPriority> priority = new ComboBox<>("Priorité (*)");
    private final ComboBox<String> responsableUsername = new ComboBox<>("Responsable (*)");
    private final Button saveButton = new Button("Enregistrer");
    private final Button cancelButton = new Button("Annuler");
    private final Button deleteButton = new Button("Supprimer");

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

        setupUI();
        configureForm();
        configureDialog();

        updateList();
        loadInitialData();
        currentTask = null;
    }

    private void setupUI() {
        // Configuration du bouton "Nouvelle tâche"
        addTaskButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTaskButton.setIcon(VaadinIcon.PLUS.create());
        addTaskButton.addClickListener(e -> {
            this.currentTask = new Task();
            openForm(currentTask);
        });

        // Configuration du champ de recherche
        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        // Configuration de la grille
        configureGrid();

        // Configuration du layout de la barre d'outils
        HorizontalLayout toolbarLayout = new HorizontalLayout(searchField, addTaskButton);
        toolbarLayout.setAlignItems(HorizontalLayout.Alignment.END);
        toolbarLayout.setWidthFull();

        // Configuration du layout principal
        VerticalLayout layout = new VerticalLayout(toolbarLayout, grid);
        layout.setPadding(false);

        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(layout);
    }

    private void configureGrid() {
        grid.addClassNames("task-grid");
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setColumnReorderingAllowed(true);

        // Configuration de l'éditeur
        editor = grid.getEditor();
        Binder<Task> gridBinder = new Binder<>(Task.class);
        editor.setBinder(gridBinder);
        editor.setBuffered(true);

        setupGridColumns(gridBinder);
        setupEditorEvents();

        // Configuration des données avec filtre de recherche
        dataView = grid.setItems();
        setupSearchFilter();
    }

    private void setupGridColumns(Binder<Task> gridBinder) {
        // Colonne Libellé
        Grid.Column<Task> libelleColumn = grid.addColumn(Task::getLibelle)
                .setHeader("Libellé")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true);
        TextField libelleField = new TextField();
        libelleField.setWidthFull();
        gridBinder.forField(libelleField)
                .asRequired("Le libellé ne peut pas être vide")
                .bind(Task::getLibelle, Task::setLibelle);
        libelleColumn.setEditorComponent(libelleField);

        // Colonne Statut
        Grid.Column<Task> statutColumn = grid.addColumn(new ComponentRenderer<>(task -> {
            StatutEnum status = task.getStatut();
            String label = status.name();
            String badgeVariant = getStatusBadgeVariant(status);

            Span badgeSpan = new Span(label);
            badgeSpan.getElement().getThemeList().add("badge " + badgeVariant);
            return badgeSpan;
        })).setHeader("Statut").setSortable(true).setAutoWidth(true).setResizable(true);
        ComboBox<StatutEnum> statutField = new ComboBox<>();
        statutField.setItems(StatutEnum.values());
        statutField.setItemLabelGenerator(StatutEnum::name);
        statutField.setWidthFull();
        gridBinder.forField(statutField)
                .asRequired("Le statut ne peut pas être vide")
                .bind(Task::getStatut, Task::setStatut);
        statutColumn.setEditorComponent(statutField);

        // Colonne Responsable
        Grid.Column<Task> responsableColumn = grid.addColumn(Task::getResponsableFullname)
                .setHeader("Responsable")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true);
        ComboBox<String> responsableField = new ComboBox<>();
        try {
            List<String> usernames = keycloakUserService.getAllUsernames();
            responsableField.setItems(usernames);
        } catch (Exception e) {
            responsableField.setItems();
        }
        responsableField.setWidthFull();
        gridBinder.forField(responsableField)
                .asRequired("Le responsable ne peut pas être vide")
                .bind(Task::getResponsableFullname, Task::setResponsableFullname);
        responsableColumn.setEditorComponent(responsableField);

        // Colonne Pays
        Grid.Column<Task> paysColumn = grid.addColumn(Task::getPaysDestinataire)
                .setHeader("Pays")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true);
        ComboBox<String> paysField = new ComboBox<>();
        try {
            List<String> countries = holidaySyncService.getAllCountries();
            paysField.setItems(countries);
        } catch (Exception e) {
            paysField.setItems("FR", "UK", "US", "DE", "ES", "IT");
        }
        paysField.setWidthFull();
        gridBinder.forField(paysField)
                .asRequired("Le pays ne peut pas être vide")
                .bind(Task::getPaysDestinataire, Task::setPaysDestinataire);
        paysColumn.setEditorComponent(paysField);

        // Colonne Priorité
        Grid.Column<Task> priorityColumn = grid.addColumn(new ComponentRenderer<>(task -> {
            TaskPriority taskPriority = task.getPriority();
            Span priorityBadge = new Span(taskPriority.name());
            String variant = getPriorityBadgeVariant(taskPriority);
            priorityBadge.getElement().getThemeList().add("badge " + variant);
            return priorityBadge;
        })).setHeader("Priorité").setSortable(true).setAutoWidth(true).setResizable(true);
        ComboBox<TaskPriority> priorityField = new ComboBox<>();
        priorityField.setItems(TaskPriority.values());
        priorityField.setItemLabelGenerator(TaskPriority::name);
        priorityField.setWidthFull();
        gridBinder.forField(priorityField)
                .asRequired("La priorité ne peut pas être vide")
                .bind(Task::getPriority, Task::setPriority);
        priorityColumn.setEditorComponent(priorityField);

        // Colonne Date limite
        Grid.Column<Task> dateLimiteColumn = grid.addColumn(Task::getDateLimite)
                .setHeader("Date limite")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true);

        // Colonne SLA
        Grid.Column<Task> slaColumn = grid.addColumn(Task::getSlaDays)
                .setHeader("SLA (jours)")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true);
        IntegerField slaField = new IntegerField();
        slaField.setMin(1);
        slaField.setWidthFull();
        gridBinder.forField(slaField)
                .asRequired("Le SLA ne peut pas être vide")
                .withValidator(val -> val != null && val >= 1, "Le SLA doit être au moins 1")
                .withConverter(
                        integer -> integer != null ? Long.valueOf(integer) : null,
                        longValue -> longValue != null ? Math.toIntExact(longValue) : null
                )
                .bind(Task::getSlaDays, Task::setSlaDays);
        slaColumn.setEditorComponent(slaField);

        // Colonne d'édition
        Grid.Column<Task> editColumn = grid.addComponentColumn(task -> {
            Button editButton = new Button("Éditer");
            editButton.addClickListener(e -> {
                if (editor.isOpen())
                    editor.cancel();
                grid.getEditor().editItem(task);
            });
            return editButton;
        }).setHeader("Actions").setResizable(true);

        Button saveGridButton = new Button("Sauvegarder", e -> {
            if (editor.save()) {
                showSuccessNotification("Tâche sauvegardée avec succès !");
            }
        });
        saveGridButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelGridButton = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
        cancelGridButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);

        HorizontalLayout actions = new HorizontalLayout(saveGridButton, cancelGridButton);
        actions.setPadding(false);
        editColumn.setEditorComponent(actions);
    }

    private void setupEditorEvents() {
        editor.addSaveListener(event -> {
            try {
                Task task = event.getItem();
                Task savedTask = taskService.update(task);
                updateList();
                showSuccessNotification("Tâche mise à jour avec succès !");
            } catch (Exception e) {
                showErrorNotification("Erreur lors de la sauvegarde : " + e.getMessage());
                editor.cancel();
            }
        });
    }

    private void setupSearchFilter() {
        searchField.addValueChangeListener(e -> {
            if (dataView != null) {
                dataView.refreshAll();
            }
        });

        dataView.addFilter(task -> {
            String searchTerm = searchField.getValue();
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return true;
            }

            searchTerm = searchTerm.trim().toLowerCase();
            boolean matchesLibelle = task.getLibelle().toLowerCase().contains(searchTerm);
            boolean matchesDescription = task.getDescription().toLowerCase().contains(searchTerm);
            boolean matchesResponsable = task.getResponsableFullname().toLowerCase().contains(searchTerm);
            boolean matchesPays = task.getPaysDestinataire().toLowerCase().contains(searchTerm);

            return matchesLibelle || matchesDescription || matchesResponsable || matchesPays;
        });
    }

    private void configureForm() {
        configureFormFields();
        configureBinder();
        configureFormButtons();
        configureFormLayout();
    }

    private void configureFormFields() {
        libelle.setPlaceholder("Titre de la tâche");
        libelle.setRequired(true);

        statut.setItems(StatutEnum.values());
        statut.setItemLabelGenerator(StatutEnum::name);
        statut.setPlaceholder("Sélectionner un statut");
        statut.setValue(StatutEnum.A_FAIRE);
        statut.setRequired(true);

        description.setPlaceholder("Description détaillée de la tâche");
        description.setHeight("120px");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setRequired(true);

        slaDays.setMin(1);
        slaDays.setStep(1);
        slaDays.setPlaceholder("Nombre de jours pour le SLA");
        slaDays.setRequiredIndicatorVisible(true);

        paysDestinataire.setPlaceholder("Sélectionner un pays");
        paysDestinataire.setRequired(true);

        dateLimite.setPlaceholder("jj/mm/aaaa");
        dateLimite.setLocale(Locale.FRANCE);
        dateLimite.setReadOnly(true);

        priority.setItems(TaskPriority.values());
        priority.setItemLabelGenerator(TaskPriority::name);
        priority.setPlaceholder("Sélectionner une priorité");
        priority.setValue(TaskPriority.NORMALE);
        priority.setRequired(true);

        responsableUsername.setPlaceholder("Sélectionner un responsable");
        responsableUsername.setRequired(true);

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
                .bind(Task::getResponsableFullname, Task::setResponsableFullname);
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
            System.out.println(countries);
            if (countries != null && !countries.isEmpty()) {
                paysDestinataire.setItems(countries);
            } else {
                paysDestinataire.setItems("FR", "UK", "US", "DE", "ES", "IT");
                paysDestinataire.setValue("FR");
            }
        } catch (Exception e) {
            paysDestinataire.setItems("FR", "UK", "US", "DE", "ES", "IT");
            paysDestinataire.setValue("FR");
        }
    }

    private void loadResponsibleUsers() {
        try {
            List<String> usernames = keycloakUserService.getAllUsernames();
            responsableUsername.setItems(usernames);
        } catch (Exception e) {
            responsableUsername.setItems();
        }
    }

    private void openForm(Task task) {
        this.currentTask = task;

        if (task.getId() == null) {
            task.setCreationDate(Instant.now());
            task.setStatut(StatutEnum.A_FAIRE);
            task.setPriority(TaskPriority.NORMALE);
            deleteButton.setVisible(false);
        } else {
            deleteButton.setVisible(true);
        }

        binder.setBean(task);
        updateDueDate();

        formDialog.setHeaderTitle(task.getId() == null ? "Nouvelle tâche" : "Modifier la tâche : " + task.getLibelle());
        formDialog.open();
    }

    private void closeForm() {
        formDialog.close();
        currentTask = null;
        binder.setBean(null);
    }

    private void saveTask() {
        try {
            if (binder.writeBeanIfValid(currentTask)) {
                boolean isNew = currentTask.getId() == null;
                Task savedTask = isNew ? taskService.save(currentTask, responsableUsername.getValue()) : taskService.update(currentTask);

                updateList();
                closeForm();

                String message = isNew ? "Tâche créée avec succès !" : "Tâche mise à jour avec succès !";
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
        if (currentTask.getId() != null) {
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
            if (slaDays.getValue() != null) {
                LocalDate startDate;
                startDate = currentTask.getCreationDate().atZone(ZoneId.systemDefault()).toLocalDate();

                String countryCode = holidaySyncService.countryCodeSetup(paysDestinataire.getValue());
                LocalDate computedDueDate = calendarService.calculateDueDate(
                        startDate,
                        slaDays.getValue(),
                        countryCode
                );

                dateLimite.setValue(computedDueDate);
                currentTask.setDateLimite(computedDueDate);
            } else {
                dateLimite.clear();
                currentTask.setDateLimite(null);
            }
        } catch (Exception e) {
            Notification.show("Erreur lors du calcul de la date limite : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            dateLimite.clear();
            currentTask.setDateLimite(null);
        }
    }

    private void updateList() {
        try {
            List<Task> tasks = taskService.findAll();
            if (dataView != null) {
                dataView = grid.setItems(tasks);
                setupSearchFilter();
            } else {
                grid.setItems(tasks);
            }
        } catch (Exception e) {
            Notification.show("Erreur lors du chargement des tâches : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String getStatusBadgeVariant(StatutEnum status) {
        return switch (status) {
            case A_FAIRE -> "contrast";
            case EN_COURS -> "primary";
            case TERMINER -> "success";
            case SUSPENDU -> "error";
            default -> "contrast";
        };
    }

    private String getPriorityBadgeVariant(TaskPriority priority) {
        return switch (priority) {
            case FAIBLE -> "success";
            case NORMALE -> "contrast";
            case ELEVEE -> "primary";
            case CRITIQUE -> "error";
            default -> "contrast";
        };
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}