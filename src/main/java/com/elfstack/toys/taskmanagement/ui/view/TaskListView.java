//package com.elfstack.toys.taskmanagement.ui.view;
//
//import com.elfstack.toys.base.ui.component.ViewToolbar;
//import com.elfstack.toys.taskmanagement.domain.Task;
////import com.elfstack.toys.taskmanagement.service.KeycloakUserService;
//import com.elfstack.toys.taskmanagement.service.TaskService;
//import com.elfstack.toys.usermanagement.domain.KeycloakUserDto;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.datepicker.DatePicker;
//import com.vaadin.flow.component.grid.Grid;
//import com.elfstack.toys.taskmanagement.domain.StatutEnum;
//import com.vaadin.flow.component.html.Main;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.textfield.TextField;
//import com.vaadin.flow.component.textfield.TextArea;
//import com.vaadin.flow.router.Menu;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import com.vaadin.flow.theme.lumo.LumoUtility;
//import jakarta.annotation.security.PermitAll;
//
//import java.time.Clock;
//import java.time.format.DateTimeFormatter;
//import java.time.format.FormatStyle;
//import java.util.Collection;
//import java.util.Optional;
//
//@Route("task-list")
//@PageTitle("Task List")
//@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
//@PermitAll
//public class TaskListView extends Main {
//
//    private final TaskService taskService;
////    private final KeycloakUserService keycloakUserService;
//
//    final TextField libelle;
//    final TextArea description;
//    final DatePicker dueDate;
//    final DatePicker endDate;
//    final ComboBox<KeycloakUserDto> assigneeComboBox;
//    final ComboBox<StatutEnum> statutComboBox;
//    final Button createBtn;
//    final Grid<Task> taskGrid;
//
////    public TaskListView(TaskService taskService, KeycloakUserService keycloakUserService, Clock clock)
//    public TaskListView(TaskService taskService, Clock clock){
//        this.taskService = taskService;
////        this.keycloakUserService = keycloakUserService;
//
//        libelle = new TextField();
//        libelle.setPlaceholder("Libellé de la tâche");
//        libelle.setAriaLabel("Task libelle");
//        libelle.setRequired(true);
//        libelle.setMinWidth("15em");
//
//        description = new TextArea();
//        description.setPlaceholder("Description détaillée de la tâche");
//        description.setAriaLabel("Task description");
//        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
//        description.setMinWidth("20em");
//        description.setHeight("100px");
//
//        dueDate = new DatePicker();
//        dueDate.setPlaceholder("Date limite");
//        dueDate.setAriaLabel("Due date");
//        dueDate.setLabel("Date limite");
//
//
//        endDate = new DatePicker();
//        endDate.setPlaceholder("Date de fin prévue");
//        endDate.setAriaLabel("End date");
//        endDate.setLabel("Date de fin");
//
//
//        assigneeComboBox = new ComboBox<>();
//        assigneeComboBox.setLabel("Assigner à");
//        assigneeComboBox.setPlaceholder("Sélectionner un utilisateur");
//        assigneeComboBox.setItemLabelGenerator(user ->
//                String.format("%s %s (%s)",
//                        Optional.ofNullable(user.getFirstName()).orElse(""),
//                        Optional.ofNullable(user.getLastName()).orElse(""),
//                        user.getUsername()
//                ).trim()
//        );
//        assigneeComboBox.setMinWidth("15em");
//
//
//        statutComboBox = new ComboBox<>();
//        statutComboBox.setLabel("Statut");
//        statutComboBox.setPlaceholder("Sélectionner un statut");
//        statutComboBox.setItems(StatutEnum.values());
//        statutComboBox.setItemLabelGenerator(StatutEnum::name);
//        statutComboBox.setValue(StatutEnum.A_FAIRE); // Statut par défaut
//        statutComboBox.setMinWidth("12em");
//
//
////        loadUsers();
//
//
//        createBtn = new Button("Créer la tâche", event -> createTask());
//        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//
//
//        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(clock.getZone())
//                .withLocale(getLocale());
//        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());
//
//        taskGrid = new Grid<>();
////        taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
//        taskGrid.addColumn(Task::getLibelle).setHeader("Libellé").setWidth("200px");
//        taskGrid.addColumn(Task::getDescription).setHeader("Description").setWidth("300px");
////        taskGrid.addColumn(task -> Optional.ofNullable(task.getDateLimite()).map(dateFormatter::format).orElse("Aucune"))
////                .setHeader("Date limite").setWidth("150px");
//        taskGrid.addColumn(task -> Optional.ofNullable(task.getDateFin()).map(dateFormatter::format).orElse("Non définie"))
//                .setHeader("Date de fin").setWidth("150px");
//        taskGrid.addColumn(task -> Optional.of(task.getResponsableId()).map(String::valueOf).orElse("Non assigné"))
//                .setHeader("Assigné à").setWidth("150px");
////        taskGrid.addColumn(task -> Optional.of(task.getStatut()).map(Enum::name).orElse("Indéfini"))
////                .setHeader("Statut").setWidth("120px");
//        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate()))
//                .setHeader("Date de création").setWidth("180px");
//        taskGrid.setSizeFull();
//
//
//        HorizontalLayout firstRow = new HorizontalLayout(libelle, assigneeComboBox, statutComboBox);
//        firstRow.setWidthFull();
//        firstRow.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
//
//        HorizontalLayout secondRow = new HorizontalLayout(dueDate, endDate, createBtn);
//        secondRow.setWidthFull();
//        secondRow.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.END);
//
//        VerticalLayout formLayout = new VerticalLayout(firstRow, description, secondRow);
//        formLayout.setSpacing(true);
//        formLayout.setPadding(false);
//
//        setSizeFull();
//        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
//                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
//
//        add(new ViewToolbar("Liste des Tâches", formLayout));
//        add(taskGrid);
//    }
//
////    private void loadUsers() {
////        try {
//////            var users = keycloakUserService.getAllUsers();
////            assigneeComboBox.setItems((Collection<KeycloakUserDto>) users);
////        } catch (Exception e) {
////            Notification.show("Erreur lors du chargement des utilisateurs: " + e.getMessage(),
////                            5000, Notification.Position.BOTTOM_END)
////                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
////        }
////    }
//
//    private void createTask() {
//
//        if (libelle.isEmpty()) {
//            Notification.show("Le libellé est obligatoire", 3000, Notification.Position.BOTTOM_END)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//            return;
//        }
//
//        if (description.isEmpty()) {
//            Notification.show("La description est obligatoire", 3000, Notification.Position.BOTTOM_END)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//            return;
//        }
//
//        statutComboBox.getValue();
//
//        try {
//            KeycloakUserDto selectedUser = assigneeComboBox.getValue();
//            String responsableId = selectedUser.getId();
//            String responsableFirstName = selectedUser.getFirstName();
//            String responsableLastName = selectedUser.getLastName();
//
//            taskService.createTask(
//                    libelle.getValue(),
//                    description.getValue(),
//                    dueDate.getValue(),
//                    responsableId,
//                    endDate.getValue(),
//                    responsableFirstName,
//                    responsableLastName,
//                    statutComboBox.getValue()
//            );
//
//            taskGrid.getDataProvider().refreshAll();
//            clearForm();
//
//            String assigneeName = " et assignée à " + selectedUser.getUsername();
//
//            Notification.show("Tâche créée avec succès" + assigneeName, 3000, Notification.Position.BOTTOM_END)
//                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//
//        } catch (Exception e) {
//            Notification.show("Erreur lors de la création de la tâche: " + e.getMessage(),
//                            5000, Notification.Position.BOTTOM_END)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//        }
//    }
//
//    private void clearForm() {
//        libelle.clear();
//        description.clear();
//        dueDate.clear();
//        endDate.clear();
//        assigneeComboBox.clear();
//        statutComboBox.setValue(StatutEnum.A_FAIRE);
//    }
//}