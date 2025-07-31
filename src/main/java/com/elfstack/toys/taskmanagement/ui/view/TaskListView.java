package com.elfstack.toys.taskmanagement.ui.view;

import com.elfstack.toys.base.ui.component.ViewToolbar;
import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.service.TaskService;
import com.elfstack.toys.usermanagement.domain.KeycloakUserDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.elfstack.toys.taskmanagement.domain.TaskStatus;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Optional;

@Route("task-list")
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
@PermitAll
public class TaskListView extends Main {

    private final TaskService taskService;
//    private final KeycloakUserService keycloakUserService;

    final TextField title;
    final TextArea description;
    final DatePicker due_date;
    final DatePicker date_de_fin;
    final ComboBox<KeycloakUserDto> assigneeComboBox;
    final ComboBox<TaskStatus> statutComboBox;
    final Button createBtn;
    final Grid<Task> taskGrid;

//    public TaskListView(TaskService taskService, KeycloakUserService keycloakUserService, Clock clock)
    public TaskListView(TaskService taskService, Clock clock){
        this.taskService = taskService;
//        this.keycloakUserService = keycloakUserService;

        title = new TextField();
        title.setPlaceholder("Libellé de la tâche");
        title.setAriaLabel("Task title");
        title.setRequired(true);
        title.setMinWidth("15em");

        description = new TextArea();
        description.setPlaceholder("Description détaillée de la tâche");
        description.setAriaLabel("Task description");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");
        description.setHeight("100px");

        due_date = new DatePicker();
        due_date.setPlaceholder("Date limite");
        due_date.setAriaLabel("Due date");
        due_date.setLabel("Date limite");


        date_de_fin = new DatePicker();
        date_de_fin.setPlaceholder("Date de fin prévue");
        date_de_fin.setAriaLabel("End date");
        date_de_fin.setLabel("Date de fin");


        assigneeComboBox = new ComboBox<>();
        assigneeComboBox.setLabel("Assigner à");
        assigneeComboBox.setPlaceholder("Sélectionner un utilisateur");
        assigneeComboBox.setItemLabelGenerator(user ->
                String.format("%s %s (%s)",
                        Optional.ofNullable(user.getFirstName()).orElse(""),
                        Optional.ofNullable(user.getLastName()).orElse(""),
                        user.getUsername()
                ).trim()
        );
        assigneeComboBox.setMinWidth("15em");


        statutComboBox = new ComboBox<>();
        statutComboBox.setLabel("Statut");
        statutComboBox.setPlaceholder("Sélectionner un statut");
        statutComboBox.setItems(TaskStatus.values());
        statutComboBox.setItemLabelGenerator(TaskStatus::name);
        statutComboBox.setValue(TaskStatus.NOUVEAU); // Statut par défaut
        statutComboBox.setMinWidth("12em");


//        loadUsers();


        createBtn = new Button("Créer la tâche", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(clock.getZone())
                .withLocale(getLocale());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        taskGrid = new Grid<>();
//        taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        taskGrid.addColumn(Task::getTitle).setHeader("Libellé").setWidth("200px");
        taskGrid.addColumn(Task::getDescription).setHeader("Description").setWidth("300px");
//        taskGrid.addColumn(task -> Optional.ofNullable(task.getDateLimite()).map(dateFormatter::format).orElse("Aucune"))
//                .setHeader("Date limite").setWidth("150px");
        taskGrid.addColumn(task -> Optional.of(task.getDueDate()).map(dateFormatter::format).orElse("Non définie"))
                .setHeader("Date de fin").setWidth("150px");
        taskGrid.addColumn(task -> Optional.of(task.getResponsableId()).map(String::valueOf).orElse("Non assigné"))
                .setHeader("Assigné à").setWidth("150px");
//        taskGrid.addColumn(task -> Optional.of(task.getStatut()).map(Enum::name).orElse("Indéfini"))
//                .setHeader("Statut").setWidth("120px");
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreatedDate()))
                .setHeader("Date de création").setWidth("180px");
        taskGrid.setSizeFull();


        HorizontalLayout firstRow = new HorizontalLayout(title, assigneeComboBox, statutComboBox);
        firstRow.setWidthFull();
        firstRow.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);

        HorizontalLayout secondRow = new HorizontalLayout(due_date, date_de_fin, createBtn);
        secondRow.setWidthFull();
        secondRow.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);

        VerticalLayout formLayout = new VerticalLayout(firstRow, description, secondRow);
        formLayout.setSpacing(true);
        formLayout.setPadding(false);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Liste des Tâches", formLayout));
        add(taskGrid);
    }

//    private void loadUsers() {
//        try {
////            var users = keycloakUserService.getAllUsers();
//            assigneeComboBox.setItems((Collection<KeycloakUserDto>) users);
//        } catch (Exception e) {
//            Notification.show("Erreur lors du chargement des utilisateurs: " + e.getMessage(),
//                            5000, Notification.Position.BOTTOM_END)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//        }
//    }

    private void createTask() {

        if (title.isEmpty()) {
            Notification.show("Le libellé est obligatoire", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (description.isEmpty()) {
            Notification.show("La description est obligatoire", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        statutComboBox.getValue();

        try {
            KeycloakUserDto selectedUser = assigneeComboBox.getValue();
            String responsableId = selectedUser.getId();
            String responsableFirstName = selectedUser.getFirstName();
            String responsableLastName = selectedUser.getLastName();

            taskService.createTask(
                    title.getValue(),
                    description.getValue(),
                    due_date.getValue(),
                    responsableId,
                    date_de_fin.getValue(),
                    responsableFirstName,
                    responsableLastName,
                    statutComboBox.getValue()
            );

            taskGrid.getDataProvider().refreshAll();
            clearForm();

            String assigneeName = " et assignée à " + selectedUser.getUsername();

            Notification.show("Tâche créée avec succès" + assigneeName, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Erreur lors de la création de la tâche: " + e.getMessage(),
                            5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearForm() {
        title.clear();
        description.clear();
        due_date.clear();
        date_de_fin.clear();
        assigneeComboBox.clear();
        statutComboBox.setValue(TaskStatus.NOUVEAU);
    }
}