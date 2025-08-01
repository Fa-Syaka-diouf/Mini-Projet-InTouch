package com.elfstack.toys.taskmanagement.ui.view;

import com.elfstack.toys.admin.service.CalendarService;
import com.elfstack.toys.taskmanagement.domain.*;
import com.elfstack.toys.usermanagement.service.KeycloakUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Route("task-list")
@PageTitle("Task List")
@PermitAll
public class TaskForm extends FormLayout {

    private final CalendarService calendarService;
    private final KeycloakUserService keycloakUserService;

    private final TextField title = new TextField("Saisissez le libellé (*)");
    private final ComboBox<TaskStatus> status = new ComboBox<>("Statut d'avancement (*)");
    private final TextArea description = new TextArea("Saisissez la description (*)");
    private final IntegerField slaDays = new IntegerField("SLA (jours) *");
    private final ComboBox<String> country = new ComboBox<>("Sélectionnez un pays :");
    private final DatePicker dueDate = new DatePicker("La date limite calculée :");
    private final ComboBox<TaskPriority> priority = new ComboBox<>("Mettez la priorité (*)");
    private final Upload upload = new Upload();
    private final ComboBox<String> responsible = new ComboBox<>("Responsable de la tâche");

    private final Button save = new Button("Enregistrer");
    private final Button cancel = new Button("Annuler");

    private final Binder<Task> binder = new BeanValidationBinder<>(Task.class);
    private String currentUploadedFile;
    private UploadCallback uploadCallback;
    private Task task;

    public TaskForm(KeycloakUserService keycloakUserService, CalendarService calendarService) {
        this.calendarService = calendarService;
        this.keycloakUserService = keycloakUserService;

        configureFields();
        configureUpload();
        configureBinder();
        configureButtons();

        // Ajoute la liste de pays disponibles
        country.setItems(calendarService.getAvailableCountries());

        // Recalcul SLA lorsque pays ou durée change
        country.addValueChangeListener(e -> updateDueDate());
        slaDays.addValueChangeListener(e -> updateDueDate());

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
        title.setPlaceholder("Titre de la tâche");
        title.setRequired(true);

        status.setItems(TaskStatus.values());
        status.setItemLabelGenerator(TaskStatus::getDisplayName);
        status.setPlaceholder("Sélectionner un statut");
        status.setValue(TaskStatus.NOUVEAU);
        status.setRequired(true);

        description.setPlaceholder("Description détaillée de la tâche");
        description.setHeight("120px");

        slaDays.setMin(1);
        slaDays.setStep(1);
        slaDays.setPlaceholder("Nombre de jours pour le SLA");
        slaDays.setRequiredIndicatorVisible(true);

        country.setPlaceholder("Sélectionner un pays");
        country.setRequired(true);

        dueDate.setPlaceholder("jj/mm/aaaa");
        dueDate.setLocale(Locale.FRANCE);
        dueDate.setReadOnly(true);

        priority.setItems(TaskPriority.values());
        priority.setItemLabelGenerator(TaskPriority::getDisplayName);
        priority.setPlaceholder("Sélectionner une priorité");
        priority.setValue(TaskPriority.NORMALE);

        responsible.setPlaceholder("Sélectionner un responsable");
        responsible.setRequired(true);
    }

    private void configureUpload() {
        upload.setAutoUpload(true);
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setAcceptedFileTypes("application/pdf", "image/*", ".doc", ".docx");

        upload.getElement().setAttribute("drop-label", "Glissez votre fichier ici ou cliquez");
        upload.getElement().setAttribute("drop-label-icon", "lumo:upload");

        upload.getElement().addEventListener("upload-success", e -> handleUploadSuccess());
        upload.getElement().addEventListener("file-reject", e -> Notification.show("Fichier rejeté")
                .addThemeVariants(NotificationVariant.LUMO_ERROR));
        upload.getElement().addEventListener("upload-error", e -> Notification.show("Erreur upload")
                .addThemeVariants(NotificationVariant.LUMO_ERROR));

        Span info = new Span("Types acceptés : PDF, Images, DOC, DOCX (max 5MB)");
        info.getStyle().set("font-size", "0.8em");
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");
        add(info);
    }

    private void configureBinder() {
        binder.forField(title)
                .asRequired("Le titre est obligatoire")
                .withValidator(t -> t.length() >= 3, "Minimum 3 caractères")
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
                .withValidator(val -> val != null && val >= 1, "SLA minimum 1")
                .bind(Task::getSlaDays, Task::setSlaDays);
    }

    private void configureButtons() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.getStyle().set("margin-right", "var(--lumo-space-s)");
    }

    public void setTask(Task task) {
        this.task = (task == null) ? new Task() : task;
        binder.setBean(this.task);
        currentUploadedFile = null;

        if (task == null || task.getId() == null) {
            upload.clearFileList();
            upload.getElement().setProperty("files", "");
        }
    }

    public Task getTask() {
        return this.task;
    }

    public void clear() {
        setTask(new Task());
    }

    public boolean isValid() {
        return binder.validate().isOk();
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

    public void initResponsibleUsers() {
        try {
            List<String> usernames = keycloakUserService.getAllUsernames();
//            if (usernames.isEmpty()) {
//                Notification.show("Aucun utilisateur récupéré").addThemeVariants(NotificationVariant.LUMO_WARNING);
//            }
            responsible.setItems(usernames);
        } catch (Exception e) {
            responsible.setItems();
            Notification.show("Erreur chargement utilisateurs : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }


    public void setAvailableResponsibles(List<String> usernames) {
        responsible.setItems(usernames);
    }

    private void processUploadedFile(String filename, String mimeType, byte[] fileData) {
        try {
            if (uploadCallback != null && fileData.length > 0) {
                InputStream inputStream = new ByteArrayInputStream(fileData);
                currentUploadedFile = uploadCallback.onFileUploaded(inputStream, filename, mimeType);

                Task current = getTask();
                if (current != null) {
                    current.setAttachmentFilename(currentUploadedFile);
                }
            }
        } catch (Exception e) {
            getUI().ifPresent(ui -> ui.access(() -> Notification.show("Erreur fichier : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR)));
        }
    }

    private void handleUploadSuccess() {
        if (currentUploadedFile != null) {
            Notification.show("Fichier uploadé avec succès !")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }

    private void updateDueDate() {
        if (slaDays.getValue() != null && country.getValue() != null) {
            LocalDate computed = CalendarService.calculateDueDate(LocalDate.now(), slaDays.getValue(), country.getValue());
            dueDate.setValue(computed);
        }
    }

    @FunctionalInterface
    public interface UploadCallback {
        String onFileUploaded(InputStream inputStream, String fileName, String mimeType) throws Exception;
    }


}

