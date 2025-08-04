package com.elfstack.toys.taskmanagement.ui.view;

import com.elfstack.toys.usermanagement.service.KeycloakUserService;
import com.vaadin.flow.component.textfield.IntegerField;
import com.elfstack.toys.taskmanagement.domain.TaskStatus;
import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.TaskPriority;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.component.html.Span;

import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.elfstack.toys.admin.service.CalendarService;

@PermitAll
public class TaskForm extends FormLayout {
    private final CalendarService calendarService;
    private final KeycloakUserService keycloakUserService;

    private final TextField title = new TextField("Saisissez le libellé (*)");
    private final ComboBox<TaskStatus> status = new ComboBox<>("Statut d'avancement (*)");
    private final TextArea description = new TextArea("Saisissez la description (*)");
    private final IntegerField slaDays = new IntegerField("SLA (jours) *");
    private final ComboBox<String> country = new ComboBox<>("Sélectionnez un pays :");
    private final DatePicker dueDate = new DatePicker("La date limite correspondante calculée en fonction du SLA :");
    private final ComboBox<TaskPriority> priority = new ComboBox<>("Mettez la priorité (*)");
    private final Upload upload = new Upload();

    private final Button save = new Button("Enregistrer");
    private final Button cancel = new Button("Annuler");

    private final Binder<Task> binder = new BeanValidationBinder<>(Task.class);

    // Pour stocker temporairement le fichier uploadé
    private String currentUploadedFile;

    // Callback pour gérer l'upload
    private UploadCallback uploadCallback;

    private ComboBox<String> responsible = new ComboBox<>("Responsable de la tâche");

    public TaskForm(KeycloakUserService keycloakUserService, CalendarService calendarService) {
        this.calendarService = calendarService;
        this.keycloakUserService = keycloakUserService;

        configureFields();
//        configureUpload();
        configureBinder();
        configureButtons();

        // ✅ CORRECTION: Gestion d'erreur lors du calcul de la date limite
        country.addValueChangeListener(e -> updateDueDate());
        slaDays.addValueChangeListener(e -> updateDueDate());

        // ✅ CORRECTION: Gestion d'erreur lors du chargement des pays
        loadAvailableCountries();

        add(title, status, description, responsible, country, slaDays, dueDate, priority, upload,
                new HorizontalLayout(save, cancel));

        setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 2)
        );

        setColspan(description, 2);
        setColspan(upload, 2);

        binder.forField(responsible)
                .asRequired("Responsable obligatoire")
                .bind(Task::getResponsable, Task::setResponsable);
    }

    private void configureFields() {
        // Configuration des champs
        title.setPlaceholder("Titre de la tâche");
        title.setRequired(true);

        // Configuration du ComboBox statut
        status.setItems(TaskStatus.values());
        status.setItemLabelGenerator(TaskStatus::getDisplayName);
        status.setPlaceholder("Sélectionner un statut");
        status.setValue(TaskStatus.NOUVEAU);
        status.setRequired(true);

        slaDays.setMin(1);
        slaDays.setStep(1);
        slaDays.setPlaceholder("Nombre de jours pour le SLA");
        slaDays.setRequiredIndicatorVisible(true);

        description.setPlaceholder("Description détaillée de la tâche");
        description.setHeight("120px");

        // ✅ CORRECTION: Configuration du ComboBox pays sera faite via loadAvailableCountries()
        country.setPlaceholder("Sélectionner un pays");
        country.setRequired(true);

        // Configuration du DatePicker
        dueDate.setPlaceholder("jj/mm/aaaa");
        dueDate.setLocale(Locale.FRANCE);
        dueDate.setReadOnly(true);

        // Configuration du ComboBox priorité
        priority.setItems(TaskPriority.values());
        priority.setItemLabelGenerator(TaskPriority::getDisplayName);
        priority.setPlaceholder("Sélectionner une priorité");
        priority.setValue(TaskPriority.NORMALE);

        responsible.setPlaceholder("Sélectionner un responsable");
        responsible.setRequired(true);
    }

    /**
     * ✅ AJOUT: Méthode pour charger les pays disponibles avec gestion d'erreur
     */
    private void loadAvailableCountries() {
        try {
            List<String> countries = calendarService.getAvailableCountries();
            if (countries != null && !countries.isEmpty()) {
                country.setItems(countries);
                // Définir une valeur par défaut si disponible
                if (countries.contains("FR")) {
                    country.setValue("FR");
                } else {
                    country.setValue(countries.get(0));
                }
            } else {
                // Fallback avec des pays par défaut
                country.setItems("FR", "UK", "US", "DE", "ES", "IT");
                country.setValue("FR");
                Notification.show("Aucun pays configuré, utilisation des pays par défaut")
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        } catch (Exception e) {
            // Fallback en cas d'erreur
            country.setItems("FR", "UK", "US", "DE", "ES", "IT");
            country.setValue("FR");
            Notification.show("Erreur lors du chargement des pays : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void initResponsibleUsers() {
        try {
            responsible.setItems(keycloakUserService.getAllUsernames());
        } catch (Exception e) {
            responsible.setItems();
            Notification.show("Erreur lors du chargement des utilisateurs")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public ComboBox<String> getResponsibleCombo() {
        return responsible;
    }

//    private void configureUpload() {
//        upload.setAutoUpload(true);
//        upload.setMaxFiles(1);
//        upload.setMaxFileSize(5 * 1024 * 1024);
//        upload.setAcceptedFileTypes("application/pdf", "image/*", ".doc", ".docx");
//
//        upload.setReceiver(this::receiveUpload);
//
//        upload.getElement().setAttribute("drop-label", "Glissez votre fichier ici ou cliquez pour parcourir");
//        upload.getElement().setAttribute("drop-label-icon", "lumo:upload");
//        upload.getElement().setAttribute("max-file-size", "Taille maximum : 5MB");
//
//        upload.getElement().addEventListener("upload-success", event -> {
//            handleUploadSuccess();
//        });
//
//        upload.getElement().addEventListener("file-reject", event -> {
//            Notification.show("Fichier rejeté. Vérifiez le type et la taille (PDF, images, DOC, DOCX - max 5MB)")
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//        });
//
//        upload.getElement().addEventListener("upload-error", event -> {
//            Notification.show("Erreur lors de l'upload du fichier")
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//        });
//
//        Span uploadInfo = new Span("Types acceptés : PDF, Images, DOC, DOCX (max 5MB)");
//        uploadInfo.getStyle().set("font-size", "0.8em");
//        uploadInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
//        add(uploadInfo);
//    }
//    private OutputStream receiveUpload(String filename, String mimeType) {
//        return new ByteArrayOutputStream() {
//            @Override
//            public void close() throws IOException {
//                super.close();
//                byte[] fileData = toByteArray();
//                processUploadedFile(filename, mimeType, fileData);
//            }
//        };
//    }

    public void setAvailableResponsibles(List<String> usernames) {
        responsible.setItems(usernames);
    }

//    private void processUploadedFile(String filename, String mimeType, byte[] fileData) {
//        try {
//            if (uploadCallback != null && fileData.length > 0) {
//                InputStream inputStream = new ByteArrayInputStream(fileData);
//                currentUploadedFile = uploadCallback.onFileUploaded(inputStream, filename, mimeType);
//
//                Task currentTask = getTask();
//                if (currentTask != null) {
//                    currentTask.setAttachmentFilename(currentUploadedFile);
//                }
//            }
//        } catch (Exception e) {
//            getUI().ifPresent(ui -> ui.access(() -> {
//                Notification.show("Erreur lors de la sauvegarde du fichier : " + e.getMessage())
//                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
//            }));
//        }
//    }
//
//    private void handleUploadSuccess() {
//        if (currentUploadedFile != null) {
//            Notification.show("Fichier uploadé et sauvegardé avec succès !")
//                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//        }
//    }

    private void configureBinder() {
        binder.forField(title)
                .asRequired("Le titre est obligatoire")
                .withValidator(t -> t.length() >= 3, "Le titre doit contenir au moins 3 caractères")
                .bind(Task::getTitle, Task::setTitle);

        binder.forField(status)
                .asRequired("Le statut est obligatoire")
                .bind(Task::getStatus, Task::setStatus);

        binder.forField(description)
                .bind(Task::getDescription, Task::setDescription);

        binder.forField(country)
                .asRequired("Le pays est obligatoire")
                .bind(Task::getCountry, Task::setCountry);

        binder.forField(dueDate)
                .bind(Task::getDueDate, Task::setDueDate);

        binder.forField(priority)
                .bind(Task::getPriority, Task::setPriority);

        binder.forField(slaDays)
                .asRequired("Le SLA est obligatoire")
                .withValidator(val -> val != null && val >= 1, "Le SLA doit être au moins 1")
                .bind(Task::getSlaDays, Task::setSlaDays);
    }

    private void configureButtons() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.getStyle().set("margin-right", "var(--lumo-space-s)");
    }

    private Task task;

    public void setTask(Task task) {
        if (task == null) {
            clear();
            return;
        }
        this.task = task;
        binder.setBean(task);
        currentUploadedFile = null;

        // ✅ CORRECTION: Logique corrigée pour l'initialisation de l'upload
        if (task.getId() == null) {
            upload.clearFileList();
            upload.getElement().setProperty("files", "");
        }
        // ✅ CORRECTION: Suppression du else qui n'avait pas de sens

        // ✅ AJOUT: Recalculer la date limite après avoir défini la tâche
        updateDueDate();
    }

    public Task getTask() {
        return this.task;
    }

    public boolean isValid() {
        return binder.isValid();
    }

    public Button getSaveButton() {
        return save;
    }

    public Button getCancelButton() {
        return cancel;
    }

    public void setUploadCallback(UploadCallback callback) {
        this.uploadCallback = callback;
    }

    /**
     * Nettoie les ressources
     */
    public void cleanup() {
        if (currentUploadedFile != null) {
            currentUploadedFile = null;
        }
    }

    @FunctionalInterface
    public interface UploadCallback {
        String onFileUploaded(InputStream inputStream, String fileName, String mimeType) throws Exception;
    }

    public void clear() {
        this.task = new Task();
        binder.setBean(task);
        currentUploadedFile = null;
        upload.clearFileList();
        upload.getElement().setProperty("files", "");
        // ✅ AJOUT: Nettoyer aussi la date limite
        dueDate.clear();
    }

    /**
     * ✅ CORRECTION MAJEURE: Calcul de la date limite avec gestion d'erreur
     */
    private void updateDueDate() {
        try {
            if (slaDays.getValue() != null && country.getValue() != null) {
                // ✅ CORRECTION: Utiliser la date de création de la tâche si disponible
                LocalDate startDate;
                if (task != null && task.getCreatedDate() != null) {
                    startDate = task.getCreatedDate();
                } else {
                    startDate = LocalDate.now();
                }

                String countryCode = country.getValue();

                // ✅ CORRECTION: Appel de méthode non-static
                LocalDate computedDueDate = calendarService.calculateDueDate(
                        startDate,
                        slaDays.getValue(),
                        countryCode
                );

                dueDate.setValue(computedDueDate);

                // ✅ AJOUT: Mettre à jour la tâche si elle existe
                if (task != null) {
                    task.setDueDate(computedDueDate);
                }
            } else {
                // ✅ AJOUT: Nettoyer la date si les paramètres sont incomplets
                dueDate.clear();
                if (task != null) {
                    task.setDueDate(null);
                }
            }
        } catch (Exception e) {
            // ✅ AJOUT: Gestion d'erreur lors du calcul
            Notification.show("Erreur lors du calcul de la date limite : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            dueDate.clear();
            if (task != null) {
                task.setDueDate(null);
            }
        }
    }

    /**
     * ✅ AJOUT: Méthode pour forcer le recalcul de la date limite
     */
    public void recalculateDueDate() {
        updateDueDate();
    }
}