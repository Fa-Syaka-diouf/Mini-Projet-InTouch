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

import com.elfstack.toys.usermanagement.service.KeycloakUserService;
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
    private KeycloakUserService keycloakUserService = null;

    private TextField title = new TextField("Saisissez le libellé (*)");
    private ComboBox<TaskStatus> status = new ComboBox<>("Statut d'avancement (*)");
    private TextArea description = new TextArea("Saisissez la description (*)");
    private IntegerField slaDays = new IntegerField("SLA (jours) *");
    private ComboBox<String> country = new ComboBox<>("Sélectionnez un pays :");
    private DatePicker dueDate = new DatePicker("La date limite correspondante calculée en fonction du SLA :");
    private ComboBox<TaskPriority> priority = new ComboBox<>("Mettez la priorité (*)");
    private Upload upload = new Upload();

    private Button save = new Button("Enregistrer");
    private Button cancel = new Button("Annuler");

    private Binder<Task> binder = new BeanValidationBinder<>(Task.class);


    // Pour stocker temporairement le fichier uploadé
    private String currentUploadedFile;

    // Callback pour gérer l'upload
    private UploadCallback uploadCallback;

    private ComboBox<String> responsible = new ComboBox<>("Responsable de la tâche");


    public TaskForm(KeycloakUserService keycloakUserService, CalendarService calendarService) {
        this.calendarService=calendarService;
        this.keycloakUserService = keycloakUserService;
//        responsible.setItems(keycloakUserService.getAllUsernames());
        configureFields();
        configureUpload();
        configureBinder();
        configureButtons();

        // Quand on change le pays ou le SLA, recalculer
        country.addValueChangeListener(e -> updateDueDate());
        slaDays.addValueChangeListener(e -> updateDueDate());

        // Ajoute la liste de pays dans la comboBox
        country.setItems(calendarService.getAvailableCountries());

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
                .bind(Task::getResponsableUsername, Task::setResponsableUsername);


    }

    private void configureFields() {
        // Configuration des champs
        title.setPlaceholder("Titre de la tâche");
        title.setRequired(true);

        // Configuration du ComboBox statut (remplace statusLink)
        status.setItems(TaskStatus.values());
        status.setItemLabelGenerator(TaskStatus::getDisplayName);
        status.setPlaceholder("Sélectionner un statut");
        status.setValue(TaskStatus.NOUVEAU); // Valeur par défaut
        status.setRequired(true);

        slaDays.setMin(1);
        slaDays.setStep(1);
//        slaDays.setHasControls(true);
        slaDays.setPlaceholder("Nombre de jours pour le SLA");
        slaDays.setRequiredIndicatorVisible(true);

        description.setPlaceholder("Description détaillée de la tâche");
        description.setHeight("120px");

        // Configuration du ComboBox pays
        country.setItems("FR", "UK", "US", "DE", "ES", "IT");
        country.setPlaceholder("Sélectionner un pays");
        country.setRequired(true);
        country.setValue("FR");

        // Configuration du DatePicker
        dueDate.setPlaceholder("jj/mm/aaaa");
        dueDate.setLocale(Locale.FRANCE);
        dueDate.setReadOnly(true); // Calculé automatiquement

        // Configuration du ComboBox priorité
        priority.setItems(TaskPriority.values());
        priority.setItemLabelGenerator(TaskPriority::getDisplayName);
        priority.setPlaceholder("Sélectionner une priorité");
        priority.setValue(TaskPriority.NORMALE); // Valeur par défaut

        responsible.setPlaceholder("Sélectionner un responsable");
        responsible.setRequired(true);

    }

    public void initResponsibleUsers() {
        try {
            responsible.setItems(keycloakUserService.getAllUsernames());
        } catch (Exception e) {
            responsible.setItems();
            Notification.show("Erreur lors du chargement des utilisateurs").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public ComboBox<String> getResponsibleCombo() {
        return responsible;
    }

    private void configureUpload() {
        // Configuration moderne de l'upload avec Vaadin 24.8+
        upload.setAutoUpload(true);
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024); // 5MB max
        upload.setAcceptedFileTypes("application/pdf", "image/*", ".doc", ".docx");

        // Nouveau Receiver moderne
//        upload.setReceiver(this::receiveUpload);

        // Configuration des messages d'affichage
        upload.getElement().setAttribute("drop-label", "Glissez votre fichier ici ou cliquez pour parcourir");
        upload.getElement().setAttribute("drop-label-icon", "lumo:upload");
        upload.getElement().setAttribute("max-file-size", "Taille maximum : 5MB");

        // Utilisation des nouveaux événements clientside
        upload.getElement().addEventListener("upload-success", event -> {
            handleUploadSuccess();
        });

        upload.getElement().addEventListener("file-reject", event -> {
            Notification.show("Fichier rejeté. Vérifiez le type et la taille (PDF, images, DOC, DOCX - max 5MB)")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        upload.getElement().addEventListener("upload-error", event -> {
            Notification.show("Erreur lors de l'upload du fichier")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // Ajouter un indicateur visuel
        Span uploadInfo = new Span("Types acceptés : PDF, Images, DOC, DOCX (max 5MB)");
        uploadInfo.getStyle().set("font-size", "0.8em");
        uploadInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        add(uploadInfo);
    }

    /**
     * Nouveau receiver compatible Vaadin 24.8+
     */
    private OutputStream receiveUpload(String filename, String mimeType) {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();

                // Traitement asynchrone du fichier
                byte[] fileData = toByteArray();
                processUploadedFile(filename, mimeType, fileData);
            }
        };
    }

    public void setAvailableResponsibles(List<String> usernames) {
        responsible.setItems(usernames);
    }

    /**
     * Traite le fichier uploadé de manière asynchrone
     */
    private void processUploadedFile(String filename, String mimeType, byte[] fileData) {
        try {
            if (uploadCallback != null && fileData.length > 0) {
                InputStream inputStream = new ByteArrayInputStream(fileData);
                currentUploadedFile = uploadCallback.onFileUploaded(inputStream, filename, mimeType);

                // Mettre à jour la tâche avec le nom du fichier
                Task currentTask = getTask();
                if (currentTask != null) {
                    currentTask.setAttachmentFilename(currentUploadedFile);
                }
            }
        } catch (Exception e) {
            getUI().ifPresent(ui -> ui.access(() -> {
                Notification.show("Erreur lors de la sauvegarde du fichier : " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }));
        }
    }

    /**
     * Gère le succès de l'upload
     */
    private void handleUploadSuccess() {
        if (currentUploadedFile != null) {
            Notification.show("Fichier uploadé et sauvegardé avec succès !")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }

    private void configureBinder() {
        // Binding automatique des champs avec validation
        binder.forField(title)
                .asRequired("Le titre est obligatoire")
                .withValidator(t -> t.length() >= 3, "Le titre doit contenir au moins 3 caractères")
                .bind(Task::getTitle, Task::setTitle);

        binder.forField(status)
                .asRequired("Le statut est obligatoire")
                .bind(Task::getStatut, Task::setStatut);

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

        // Styles responsives
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

        if (task.getId() == null) {
            upload.clearFileList();
            upload.getElement().setProperty("files", "");
        }
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

    // Setter pour le callback d'upload
    public void setUploadCallback(UploadCallback callback) {
        this.uploadCallback = callback;
    }

    /**
    /*
     * Nettoie les ressources (optionnel)
     */
    public void cleanup() {
        if (currentUploadedFile != null) {
            // Optionnel : nettoyer les fichiers temporaires
            currentUploadedFile = null;
        }
    }

    // Interface pour gérer l'upload
    @FunctionalInterface
    public interface UploadCallback {
        String onFileUploaded(InputStream inputStream, String fileName, String mimeType) throws Exception;
    }

    public void clear() {
        this.task = new Task(); // ou null, selon logique, mais préférable d’avoir un Task vide
        binder.setBean(task);
        currentUploadedFile = null;
        upload.clearFileList();
        upload.getElement().setProperty("files", "");
    }

    private void updateDueDate() {
        if (slaDays.getValue() != null && country.getValue() != null) {
            LocalDate startDate = LocalDate.now(); // ou date de création réelle
            String countryCode = country.getValue();

            LocalDate computedDueDate = CalendarService.calculateDueDate(startDate, slaDays.getValue(), countryCode);
            dueDate.setValue(computedDueDate);
        }
    }

}