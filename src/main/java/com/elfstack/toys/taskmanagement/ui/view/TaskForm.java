//package com.elfstack.toys.taskmanagement.ui.view;
//
//import com.elfstack.toys.admin.service.HolidaySyncService;
//import com.elfstack.toys.usermanagement.service.KeycloakUserService;
//import com.vaadin.flow.component.textfield.IntegerField;
//import com.elfstack.toys.taskmanagement.domain.StatutEnum;
//import com.elfstack.toys.taskmanagement.domain.Task;
//import com.elfstack.toys.taskmanagement.domain.TaskPriority;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.datepicker.DatePicker;
//import com.vaadin.flow.component.formlayout.FormLayout;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.textfield.TextArea;
//import com.vaadin.flow.component.textfield.TextField;
//import com.vaadin.flow.component.upload.Upload;
//import com.vaadin.flow.data.binder.BeanValidationBinder;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//import com.vaadin.flow.data.binder.Binder;
//import com.vaadin.flow.component.html.Span;
//
//import jakarta.annotation.security.PermitAll;
//
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.List;
//import java.util.Locale;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//
//import com.elfstack.toys.admin.service.CalendarService;
//
//@PermitAll
//public class TaskForm extends FormLayout {
//    private final CalendarService calendarService;
//    private final KeycloakUserService keycloakUserService;
//    private final HolidaySyncService holidaySyncService;
//
//    private final TextField libelle = new TextField("Saisissez le libellé (*)");
//    private final ComboBox<StatutEnum> statut = new ComboBox<>("Statut d'avancement (*)");
//    private final TextArea description = new TextArea("Saisissez la description (*)");
//    private final IntegerField slaDays = new IntegerField("SLA (jours) *");
//    private final ComboBox<String> paysCode = new ComboBox<>("Sélectionnez un pays :");
//    private final DatePicker dateLimite = new DatePicker("La date limite correspondante calculée en fonction du SLA :");
//    private final ComboBox<TaskPriority> priority = new ComboBox<>("Mettez la priorité (*)");
////    private final Upload upload = new Upload();
//
//    private final Button save = new Button("Enregistrer");
//    private final Button cancel = new Button("Annuler");
//
//    private final Binder<Task> binder = new BeanValidationBinder<>(Task.class);
////
////    // Pour stocker temporairement le fichier uploadé
////    private String currentUploadedFile;
////
////    // Callback pour gérer l'upload
////    private UploadCallback uploadCallback;
//
//    private final ComboBox<String> responsableUsername = new ComboBox<>("Responsable de la tâche");
//
//    public TaskForm(KeycloakUserService keycloakUserService, CalendarService calendarService, HolidaySyncService holidaySyncService) {
//        this.calendarService = calendarService;
//        this.keycloakUserService = keycloakUserService;
//        this.holidaySyncService = holidaySyncService;
//
//        configureFields();
//        configureBinder();
//        configureButtons();
//
//        // Gestion d'erreur lors du calcul de la date limite
//        paysCode.addValueChangeListener(e -> updateDueDate());
//        slaDays.addValueChangeListener(e -> updateDueDate());
//
//        // Gestion d'erreur lors du chargement des pays
//        loadAvailableCountries();
//
//        add(libelle, statut, description, responsableUsername, paysCode, slaDays, dateLimite, priority,
//                new HorizontalLayout(save, cancel));
//
//        setResponsiveSteps(
//                new ResponsiveStep("0", 1),
//                new ResponsiveStep("500px", 2)
//        );
//
//        setColspan(description, 2);
////        setColspan(upload, 2);
//
//        binder.forField(responsableUsername)
//                .asRequired("Responsable obligatoire")
//                .bind(Task::getResponsableUsername, Task::setResponsableUsername);
//    }
//
//    private void configureFields() {
//        libelle.setPlaceholder("Titre de la tâche");
//        libelle.setRequired(true);
//
//        statut.setItems(StatutEnum.values());
//        statut.setItemLabelGenerator(StatutEnum::name); // Ou créer une méthode getDisplayName() dans StatutEnum
//        statut.setPlaceholder("Sélectionner un statut");
//        statut.setValue(StatutEnum.A_FAIRE);
//        statut.setRequired(true);
//
//
//        slaDays.setMin(1);
//        slaDays.setStep(1);
//        slaDays.setPlaceholder("Nombre de jours pour le SLA");
//        slaDays.setRequiredIndicatorVisible(true);
//
//        description.setPlaceholder("Description détaillée de la tâche");
//        description.setHeight("120px");
//        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
//
//
//        paysCode.setPlaceholder("Sélectionner un pays");
//        paysCode.setRequired(true);
//
//
//        dateLimite.setPlaceholder("jj/mm/aaaa");
//        dateLimite.setLocale(Locale.FRANCE);
//        dateLimite.setReadOnly(true);
//
//
//        priority.setItems(TaskPriority.values());
//        priority.setItemLabelGenerator(TaskPriority::name);
//        priority.setPlaceholder("Sélectionner une priorité");
//        priority.setValue(TaskPriority.NORMALE);
//
//        responsableUsername.setPlaceholder("Sélectionner un responsable");
//        responsableUsername.setRequired(true);
//    }
//
//    /**
//     * Méthode pour charger les pays disponibles avec gestion d'erreur
//     */
//    private void loadAvailableCountries() {
//        try {
//            List<String> countries = holidaySyncService.getAllCountries();
//            if (countries != null && !countries.isEmpty()) {
////                paysCode.setItems(holidaySyncService.countryCodeSetup(countries));
//                // Définir une valeur par défaut si disponible
//                if (countries.contains("FR")) {
//                    paysCode.setValue("FR");
//                } else {
//                    paysCode.setValue(countries.get(0));
//                }
//            } else {
//                // Fallback avec des pays par défaut
//                paysCode.setItems("FR", "UK", "US", "DE", "ES", "IT");
//                paysCode.setValue("FR");
//                Notification.show("Aucun pays configuré, utilisation des pays par défaut")
//                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
//            }
//        } catch (Exception e) {
//            // Fallback en cas d'erreur
//            paysCode.setItems("FR", "UK", "US", "DE", "ES", "IT");
//            paysCode.setValue("FR");
//            Notification.show("Erreur lors du chargement des pays : " + e.getMessage())
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//        }
//    }
//
//    public void initResponsibleUsers() {
//        try {
//            responsableUsername.setItems(keycloakUserService.getAllUsernames());
//        } catch (Exception e) {
//            responsableUsername.setItems();
//            Notification.show("Erreur lors du chargement des utilisateurs")
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//        }
//    }
//
//    public ComboBox<String> getResponsibleCombo() {
//        return responsableUsername;
//    }
//
//    public void setAvailableResponsibles(List<String> usernames) {
//        responsableUsername.setItems(usernames);
//    }
//
//    private void configureBinder() {
//        // CORRECTION: Bind avec le bon nom de propriété
//        binder.forField(libelle)
//                .asRequired("Le libellé est obligatoire")
//                .withValidator(t -> t.length() >= 3, "Le libellé doit contenir au moins 3 caractères")
//                .bind(Task::getLibelle, Task::setLibelle);
//
//        // CORRECTION: Bind avec StatutEnum
//        binder.forField(statut)
//                .asRequired("Le statut est obligatoire")
//                .bind(Task::getStatut, Task::setStatut);
//
//        binder.forField(description)
//                .bind(Task::getDescription, Task::setDescription);
//
//        // CORRECTION: Bind avec paysCode - Attention: Task n'a pas de propriété paysCode
//        // Il faut l'ajouter au modèle Task ou gérer différemment
//        // Pour l'instant, on commente cette ligne car la propriété n'existe pas dans Task
//        // binder.forField(paysCode)
//        //         .asRequired("Le pays est obligatoire")
//        //         .bind(Task::getPaysCode, Task::setPaysCode);
//
//        // CORRECTION: Bind avec dateLimite
//        binder.forField(dateLimite)
//                .bind(Task::getDateLimite, Task::setDateLimite);
//
//        binder.forField(priority)
//                .bind(Task::getPriority, Task::setPriority);
//
//        // CORRECTION: Conversion Integer vers Long pour slaDays
//        binder.forField(slaDays)
//                .asRequired("Le SLA est obligatoire")
//                .withValidator(val -> val != null && val >= 1, "Le SLA doit être au moins 1")
//                .withConverter(
//                        integer -> integer != null ? Long.valueOf(integer) : null,
//                        longValue -> longValue != null ? Math.toIntExact(longValue) : null
//                )
//                .bind(Task::getSlaDays, Task::setSlaDays);
//    }
//
//    private void configureButtons() {
//        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
//        save.getStyle().set("margin-right", "var(--lumo-space-s)");
//    }
//
//    private Task task;
//
//    public void setTask(Task task) {
//        if (task == null) {
//            clear();
//            return;
//        }
//        this.task = task;
//        binder.setBean(task);
//
//        // Recalculer la date limite après avoir défini la tâche
//        updateDueDate();
//    }
//
//    public Task getTask() {
//        return this.task;
//    }
//
//    public boolean isValid() {
//        return binder.isValid();
//    }
//
//    public Button getSaveButton() {
//        return save;
//    }
//
//    public Button getCancelButton() {
//        return cancel;
//    }
//
//
//
//    public void clear() {
//        this.task = new Task();
//        binder.setBean(task);
//        // Nettoyer aussi la date limite
//        dateLimite.clear();
//    }
//
//    /**
//     * CORRECTION MAJEURE: Calcul de la date limite avec gestion d'erreur
//     * Compatible avec CalendarService qui prend int slaDays et non Long
//     */
//    private void updateDueDate() {
//        try {
//            if (slaDays.getValue() != null && paysCode.getValue() != null) {
//                // Utiliser la date de création de la tâche si disponible
//                LocalDate startDate;
//                if (task != null && task.getCreationDate() != null) {
//                    // CORRECTION: Convertir Instant en LocalDate
//                    startDate = task.getCreationDate().atZone(ZoneId.systemDefault()).toLocalDate();
//                } else {
//                    startDate = LocalDate.now();
//                }
//
//                String countryCode = paysCode.getValue();
//
//                // CORRECTION: CalendarService.calculateDueDate prend un int, pas un Long
//                LocalDate computedDueDate = calendarService.calculateDueDate(
//                        startDate,
//                        slaDays.getValue(), // IntegerField retourne Integer, CalendarService attend int
//                        countryCode
//                );
//
//                dateLimite.setValue(computedDueDate);
//
//                // Mettre à jour la tâche si elle existe
//                if (task != null) {
//                    task.setDateLimite(computedDueDate);
//                }
//            } else {
//                // Nettoyer la date si les paramètres sont incomplets
//                dateLimite.clear();
//                if (task != null) {
//                    task.setDateLimite(null);
//                }
//            }
//        } catch (Exception e) {
//            // Gestion d'erreur lors du calcul
//            Notification.show("Erreur lors du calcul de la date limite : " + e.getMessage())
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//            dateLimite.clear();
//            if (task != null) {
//                task.setDateLimite(null);
//            }
//        }
//    }
//
//    /**
//     * Méthode pour forcer le recalcul de la date limite
//     */
//    public void recalculateDueDate() {
//        updateDueDate();
//    }
//}