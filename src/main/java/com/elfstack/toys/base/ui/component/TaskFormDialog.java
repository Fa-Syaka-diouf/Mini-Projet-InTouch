package com.elfstack.toys.base.ui.component;

import com.elfstack.toys.admin.service.CalendarService;
import com.elfstack.toys.admin.service.HolidaySyncService;
import com.elfstack.toys.security.dev.DevSecurityService;
import com.elfstack.toys.taskmanagement.domain.StatutEnum;
import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.TaskPriority;
import com.elfstack.toys.taskmanagement.service.TaskService;
import com.elfstack.toys.usermanagement.service.KeycloakUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Composant modulaire pour la création et modification de tâches.
 * Peut être utilisé dans n'importe quelle partie de l'application.
 */
@Component
public class TaskFormDialog extends Dialog {

    // Services injectés
    private final TaskService taskService;
    private final CalendarService calendarService;
    private final KeycloakUserService keycloakUserService;
    private final HolidaySyncService holidaySyncService;
    private final DevSecurityService devSecurityService;

    // Composants du formulaire
    private final FormLayout formLayout = new FormLayout();
    private final TextField libelle = new TextField("Libellé (*)");
    private final ComboBox<StatutEnum> statut = new ComboBox<>("Statut (*)");
    private final TextArea description = new TextArea("Description (*)");
    private final IntegerField slaDays = new IntegerField("SLA (jours) *");
    private final ComboBox<String> paysDestinataire = new ComboBox<>("Pays destinataire (*)");
    private final DatePicker dateLimite = new DatePicker("Date limite calculée");
    private final ComboBox<TaskPriority> priority = new ComboBox<>("Priorité (*)");
    private final Button saveButton = new Button("Enregistrer");
    private final Button cancelButton = new Button("Annuler");
    private final Button deleteButton = new Button("Supprimer");

    // Binder et état
    private final Binder<Task> binder = new BeanValidationBinder<>(Task.class);
    /**
     * -- GETTER --
     *
     * @return La tâche actuellement en cours d'édition, ou null
     */
    @Getter
    private Task currentTask;

    /**
     * -- SETTER --
     *  Définit le callback à exécuter quand une tâche est sauvegardée
     *
     * @param onTaskSaved Consumer qui reçoit la tâche sauvegardée
     */
    // Callbacks pour les événements
    @Setter
    private Consumer<Task> onTaskSaved;
    /**
     * -- SETTER --
     *  Définit le callback à exécuter quand une tâche est supprimée
     *
     * @param onTaskDeleted Consumer qui reçoit la tâche supprimée
     */
    @Setter
    private Consumer<Task> onTaskDeleted;
    /**
     * -- SETTER --
     *  Définit le callback à exécuter quand le formulaire est fermé
     *
     * @param onFormClosed Runnable à exécuter
     */
    @Setter
    private Runnable onFormClosed;

    public TaskFormDialog(TaskService taskService,
                          CalendarService calendarService,
                          KeycloakUserService keycloakUserService,
                          HolidaySyncService holidaySyncService,
                          DevSecurityService devSecurityService) {
        this.taskService = taskService;
        this.calendarService = calendarService;
        this.keycloakUserService = keycloakUserService;
        this.holidaySyncService = holidaySyncService;
        this.devSecurityService = devSecurityService;

        initializeDialog();
        configureFormFields();
        configureBinder();
        configureFormButtons();
        configureFormLayout();
        loadInitialData();
    }

    /**
     * Initialise les propriétés du dialog
     */
    private void initializeDialog() {
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("800px");
        setHeight("600px");
        add(formLayout);
    }

    /**
     * Configure les champs du formulaire
     */
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
        paysDestinataire.setReadOnly(true);

        dateLimite.setPlaceholder("jj/mm/aaaa");
        dateLimite.setLocale(Locale.FRANCE);
        dateLimite.setReadOnly(true);

        priority.setItems(TaskPriority.values());
        priority.setItemLabelGenerator(TaskPriority::name);
        priority.setPlaceholder("Sélectionner une priorité");
        priority.setValue(TaskPriority.MOYENNE);
        priority.setRequired(true);

        // Listeners pour le calcul automatique de la date limite
        paysDestinataire.addValueChangeListener(e -> updateDueDate());
        slaDays.addValueChangeListener(e -> updateDueDate());
    }

    /**
     * Configure le binder pour la validation
     */
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
    }

    /**
     * Configure les boutons du formulaire
     */
    private void configureFormButtons() {
        saveButton.getStyle().setBackgroundColor("#243163");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        saveButton.addClassName("save-button-custom");
        cancelButton.addClassName("bouton-custom");

        saveButton.getStyle().set("margin-right", "var(--lumo-space-s)");
        deleteButton.getStyle().set("margin-left", "auto");

        saveButton.addClickListener(e -> saveTask());
        cancelButton.addClickListener(e -> closeForm());
        deleteButton.addClickListener(e -> deleteTask());
    }

    /**
     * Configure la mise en page du formulaire
     */
    private void configureFormLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton, deleteButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        buttonLayout.setWidthFull();

        formLayout.add(
                libelle,
                statut,
                description,
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

    /**
     * Charge les données initiales (pays disponibles)
     */
    private void loadInitialData() {
        try {
            List<String> countries = holidaySyncService.getAllCountries();
            if (countries != null && !countries.isEmpty()) {
                paysDestinataire.setItems(countries);
            } else {
                paysDestinataire.setItems("FR", "UK", "US", "DE", "ES", "IT");
                paysDestinataire.setValue("FR");
            }
        } catch (Exception e) {
            paysDestinataire.setItems("FR", "UK", "US", "DE", "ES", "IT");
            paysDestinataire.setValue("FR");
            showErrorNotification("Erreur lors du chargement des pays : " + e.getMessage());
        }
    }

    /**
     * Ouvre le formulaire pour une nouvelle tâche
     */
    public void openForNewTask() {
        Task newTask = new Task();
        newTask.setCreationDate(Instant.now());
        newTask.setStatut(StatutEnum.A_FAIRE);
        newTask.setPriority(TaskPriority.MOYENNE);
        newTask.setResponsableUsername(devSecurityService.getCurrentUsername());

        openForm(newTask);
    }

    /**
     * Ouvre le formulaire pour modifier une tâche existante
     * @param task La tâche à modifier
     */
    public void openForEditTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("La tâche ne peut pas être null");
        }
        openForm(task);
    }

    /**
     * Ouvre le formulaire avec la tâche spécifiée
     * @param task La tâche à éditer
     */
    private void openForm(Task task) {
        this.currentTask = task;

        boolean isNewTask = task.getId() == null;
        deleteButton.setVisible(!isNewTask);

        binder.setBean(task);
        updateCountryFromUser();
        updateDueDate();

        setHeaderTitle(isNewTask ? "Nouvelle tâche" : "Modifier la tâche : " + task.getLibelle());
        open();
    }

    /**
     * Ferme le formulaire
     */
    private void closeForm() {
        close();
        currentTask = null;
        binder.setBean(null);

        if (onFormClosed != null) {
            onFormClosed.run();
        }
    }

    /**
     * Sauvegarde la tâche
     */
    private void saveTask() {
        try {
            if (binder.writeBeanIfValid(currentTask)) {
                boolean isNew = currentTask.getId() == null;

                Task savedTask;
                if (isNew) {
                    currentTask.setResponsableUsername(devSecurityService.getCurrentUsername());
                    savedTask = taskService.save(currentTask);
                } else {
                    savedTask = taskService.update(currentTask);
                }

                String message = isNew ? "Tâche créée avec succès !" : "Tâche mise à jour avec succès !";
                showSuccessNotification(message);

                // Callback pour notifier le composant parent
                if (onTaskSaved != null) {
                    onTaskSaved.accept(savedTask);
                }

                closeForm();
            } else {
                showErrorNotification("Veuillez corriger les erreurs dans le formulaire");
            }
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Supprime la tâche
     */
    private void deleteTask() {
        if (currentTask != null && currentTask.getId() != null) {
            try {
                taskService.delete(currentTask.getId());
                showSuccessNotification("Tâche supprimée avec succès !");

                // Callback pour notifier le composant parent
                if (onTaskDeleted != null) {
                    onTaskDeleted.accept(currentTask);
                }

                closeForm();
            } catch (Exception e) {
                showErrorNotification("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    /**
     * Met à jour la date limite en fonction du SLA et du pays
     */
    private void updateDueDate() {
        try {
            if (currentTask == null || slaDays.getValue() == null) {
                dateLimite.clear();
                return;
            }

            LocalDate startDate = currentTask.getCreationDate()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String countryCode = holidaySyncService.countryCodeSetup(paysDestinataire.getValue());
            LocalDate computedDueDate = calendarService.calculateDueDate(
                    startDate,
                    slaDays.getValue(),
                    countryCode
            );

            dateLimite.setValue(computedDueDate);
            currentTask.setDateLimite(computedDueDate);

        } catch (Exception e) {
            showErrorNotification("Erreur lors du calcul de la date limite : " + e.getMessage());
            dateLimite.clear();
            if (currentTask != null) {
                currentTask.setDateLimite(null);
            }
        }
    }

    private void updateCountryFromUser() {
        try {
            if (currentTask == null || devSecurityService.getCurrentUsername() == null) {
                paysDestinataire.clear();
                return;
            }

            String country = keycloakUserService.getCountryByUserName(devSecurityService.getCurrentUsername());
            paysDestinataire.setValue(country);
            currentTask.setPaysDestinataire(country);

        } catch (Exception e) {
            showErrorNotification("Erreur lors de la récupération du pays : " + e.getMessage());
            paysDestinataire.clear();
            if (currentTask != null) {
                currentTask.setPaysDestinataire(null);
            }
        }
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public boolean isNewTask() {
        return currentTask.getId() == null;
    }
    public boolean isFormOpen() {
        return isOpened();
    }
}