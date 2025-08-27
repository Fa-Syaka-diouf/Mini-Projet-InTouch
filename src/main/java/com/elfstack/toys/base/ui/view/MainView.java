package com.elfstack.toys.base.ui.view;

import com.elfstack.toys.admin.service.CalendarService;
import com.elfstack.toys.admin.service.HolidaySyncService;

import com.elfstack.toys.base.ui.component.TaskFormDialog;
import com.elfstack.toys.security.dev.DevSecurityService;
import com.elfstack.toys.taskmanagement.domain.StatutEnum;
import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.TaskPriority;
import com.elfstack.toys.taskmanagement.service.TaskService;
import com.elfstack.toys.usermanagement.service.KeycloakUserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route (value = "" , layout = MainLayout.class)
@RolesAllowed({"ADMIN", "USER"})
public final class MainView extends VerticalLayout {

    private final TaskService taskService;
    private final DevSecurityService devSecurityService;
    private final TaskFormDialog taskFormDialog;
    private final Grid<Task> grid_task_missed = new Grid<>(Task.class, false);
    private final Grid<Task> grid_task_upcomming = new Grid<>(Task.class, false);
    private H3 missedTitle;
    private H3 upcomingTitle;
    private Card card_missing_task = new Card();
    private Card card_upcoming_task = new Card();

    // KPI Cards elements
    private Card kpiTotalTasks = new Card();
    private Card kpiMissedTasks = new Card();
    private Card kpiCompletedTasks = new Card();
    private Card kpiCreateTask = new Card();

    private H2 totalTasksValue = new H2("0");
    private H2 missedTasksValue = new H2("0");
    private H2 completedTasksValue = new H2("0");

    public MainView(TaskService taskService,
                    KeycloakUserService keycloakUserService,
                    CalendarService calendarService,
                    HolidaySyncService holidaySyncService,
                    DevSecurityService devSecurityService,
                    TaskFormDialog taskFormDialog) {
        this.taskService = taskService;
        this.devSecurityService = devSecurityService;
        this.taskFormDialog = taskFormDialog;

        taskFormDialog.setOnTaskSaved(this::onTaskCreated);
        taskFormDialog.setOnFormClosed(this::onFormClosed);
        addClassName("task-management-view");
        setSizeFull();
        setupUI();
        updateDashboardGrids();
    }

    private void setupUI() {
        configureOtherGrid();

        H1 title = new H1("Dashboard");
        title.addClassNames(LumoUtility.Margin.Bottom.LARGE);
        title.getStyle().setColor("#243163");
        HorizontalLayout header = new HorizontalLayout(title);
        header.setMargin(false);

        // Setup KPI Cards
        HorizontalLayout kpiLayout = setupKpiCards();

        missedTitle = new H3("Tâches en retard (0)");
        missedTitle.getStyle().set("color", "#a51b1b").set("margin", "10px");

        upcomingTitle = new H3("À Faire dans la semaine (0)");
        upcomingTitle.getStyle().set("color", "#243163").set("margin", "10px");

        VerticalLayout missedLayout = new VerticalLayout(missedTitle, grid_task_missed);
        missedLayout.setPadding(false);
        missedLayout.setSpacing(false);
        missedLayout.getStyle().set("height","300px");

        VerticalLayout upcomingLayout = new VerticalLayout(upcomingTitle, grid_task_upcomming);
        upcomingLayout.setPadding(false);
        upcomingLayout.getStyle().set("height","300px");
        upcomingLayout.setSpacing(false);

        card_missing_task.removeAll();
        card_missing_task.add(missedLayout);
        card_missing_task.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid rgba(0, 0, 0, 0.1)")
                .set("border-radius", "10px")
                .set("height", "350px")
                .set("width","100%")
                .set("min-height", "250px");

        card_upcoming_task.removeAll();
        card_upcoming_task.add(upcomingLayout);
        card_upcoming_task.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid rgba(0, 0, 0, 0.1)")
                .set("border-radius", "10px")
                .set("height", "350px")
                .set("width","100%")
                .set("min-height", "250px");

        HorizontalLayout cardLayout = new HorizontalLayout(card_missing_task, card_upcoming_task);
        cardLayout.setMargin(true);
        cardLayout.setSpacing(true);
        cardLayout.setSizeFull();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.setFlexGrow(0, header);
        mainLayout.setFlexGrow(0, kpiLayout);
        mainLayout.setFlexGrow(1, cardLayout);

        mainLayout.add(header, kpiLayout, cardLayout);

        add(mainLayout);

        addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Height.FULL,
                LumoUtility.Width.FULL
        );
    }

    private HorizontalLayout setupKpiCards() {
        // KPI Card 1: Total Tasks
        Icon tasksIcon = VaadinIcon.TASKS.create();
        tasksIcon.setSize("24px");
        tasksIcon.getStyle().set("color", "#243163");

        Span totalTasksLabel = new Span("Total des tâches");
        totalTasksLabel.getStyle()
                .set("font-size", "14px")
                .set("color", "#666")
                .set("margin-bottom", "5px");

        totalTasksValue.getStyle()
                .set("color", "#243163")
                .set("margin", "0")
                .set("font-weight", "bold");

        VerticalLayout totalTasksContent = new VerticalLayout(
                new HorizontalLayout(tasksIcon, totalTasksLabel),
                totalTasksValue
        );
        totalTasksContent.setSpacing(false);
        totalTasksContent.setPadding(false);

        kpiTotalTasks.add(totalTasksContent);
        setupKpiCardStyle(kpiTotalTasks, "#f8f9fa");

        // KPI Card 2: Missed Tasks
        Icon missedIcon = VaadinIcon.CLOCK.create();
        missedIcon.setSize("24px");
        missedIcon.getStyle().set("color", "#a51b1b");

        Span missedTasksLabel = new Span("En retard");
        missedTasksLabel.getStyle()
                .set("font-size", "14px")
                .set("color", "#666")
                .set("margin-bottom", "5px");

        missedTasksValue.getStyle()
                .set("color", "#a51b1b")
                .set("margin", "0")
                .set("font-weight", "bold");

        VerticalLayout missedTasksContent = new VerticalLayout(
                new HorizontalLayout(missedIcon, missedTasksLabel),
                missedTasksValue
        );
        missedTasksContent.setSpacing(false);
        missedTasksContent.setPadding(false);

        kpiMissedTasks.add(missedTasksContent);
        setupKpiCardStyle(kpiMissedTasks, "#ffebee");

        // KPI Card 3: Completed Tasks
        Icon completedIcon = VaadinIcon.CHECK_CIRCLE.create();
        completedIcon.setSize("24px");
        completedIcon.getStyle().set("color", "#28a745");

        Span completedTasksLabel = new Span("Terminées");
        completedTasksLabel.getStyle()
                .set("font-size", "14px")
                .set("color", "#666")
                .set("margin-bottom", "5px");

        completedTasksValue.getStyle()
                .set("color", "#28a745")
                .set("margin", "0")
                .set("font-weight", "bold");

        VerticalLayout completedTasksContent = new VerticalLayout(
                new HorizontalLayout(completedIcon, completedTasksLabel),
                completedTasksValue
        );
        completedTasksContent.setSpacing(false);
        completedTasksContent.setPadding(false);

        kpiCompletedTasks.add(completedTasksContent);
        setupKpiCardStyle(kpiCompletedTasks, "#e8f5e8");

        // KPI Card 4: Create Task Button
        Button createTaskButton = new Button("Créer une tâche", VaadinIcon.PLUS.create());
        createTaskButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createTaskButton.addClassName("create-bouton");
        createTaskButton.getStyle()
                .set("width", "100%")
                .set("height", "60px")
                .set("font-weight", "bold")
                .set("border-radius", "8px");
        createTaskButton.addClickListener(e -> {
            taskFormDialog.openForNewTask();
        });

        Span createTaskLabel = new Span("Actions rapides");
        createTaskLabel.getStyle()
                .set("font-size", "14px")
                .set("color", "#666")
                .set("margin-bottom", "10px");

        VerticalLayout createTaskContent = new VerticalLayout(createTaskLabel, createTaskButton);
        createTaskContent.setSpacing(false);
        createTaskContent.setPadding(false);
        createTaskContent.setAlignItems(Alignment.STRETCH);

        kpiCreateTask.add(createTaskContent);
        setupKpiCardStyle(kpiCreateTask, "#e3f2fd");

        // Layout for KPI cards
        HorizontalLayout kpiLayout = new HorizontalLayout(
                kpiTotalTasks, kpiMissedTasks, kpiCompletedTasks, kpiCreateTask
        );
        kpiLayout.setSpacing(true);
        kpiLayout.setMargin(true);
        kpiLayout.setWidthFull();
        kpiLayout.getStyle().set("margin-bottom", "20px");

        return kpiLayout;
    }

    private void setupKpiCardStyle(Card card, String backgroundColor) {
        card.getStyle()
                .set("background-color", backgroundColor)
                .set("border", "1px solid rgba(0, 0, 0, 0.1)")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("min-height", "120px")
                .set("width", "100%")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("transition", "box-shadow 0.3s ease");

        // Add hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("box-shadow", "0 4px 8px rgba(0,0,0,0.15)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        });
    }

    private void updateKpiValues() {
        try {
            String currentUsername = devSecurityService.getCurrentUsername();

            List<Task> allUserTasks = taskService.getTasksByResponsable(currentUsername);
            int totalTasks = allUserTasks.size();

            List<Task> missedTasks = taskService.getTasksEnRetard()
                    .stream()
                    .filter(task -> currentUsername.equals(task.getResponsableUsername()))
                    .toList();
            int missedCount = missedTasks.size();

            List<Task> completedTasks = taskService.getByStatut(StatutEnum.TERMINER);
            int completedCount = completedTasks.size();

            // Update UI with access wrapper
            UI.getCurrent().access(() -> {
                totalTasksValue.setText(String.valueOf(totalTasks));
                missedTasksValue.setText(String.valueOf(missedCount));
                completedTasksValue.setText(String.valueOf(completedCount));
            });

        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des KPI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateDashboardTitles() {
        int missedCount = grid_task_missed.getListDataView().getItemCount();
        int upcomingCount = grid_task_upcomming.getListDataView().getItemCount();

        missedTitle.setText("Tâches en retard (" + missedCount + ")");
        upcomingTitle.setText("À Faire dans la semaine (" + upcomingCount + ")");

        System.out.println("Mise à jour des titres - Retard: " + missedCount + ", À venir: " + upcomingCount);
    }

    private void updateDashboardGrids() {
        try {
            String currentUsername = devSecurityService.getCurrentUsername();

            List<Task> tachesRetard = taskService.getTasksEnRetard()
                    .stream()
                    .filter(task -> currentUsername.equals(task.getResponsableUsername()))
                    .toList();

            List<Task> tachesUpcoming = taskService.getTasksAVenir()
                    .stream()
                    .filter(task -> currentUsername.equals(task.getResponsableUsername()))
                    .toList();

            grid_task_missed.setItems(tachesRetard);
            grid_task_upcomming.setItems(tachesUpcoming);

            UI.getCurrent().access(() -> {
                updateDashboardTitles();
                updateKpiValues(); // Update KPI values as well
            });

            // Optionnel : afficher un message si les listes sont vides
            if (tachesRetard.isEmpty()) {
                System.out.println("Aucune tâche en retard trouvée pour l'utilisateur: " + currentUsername);
            }
            if (tachesUpcoming.isEmpty()) {
                System.out.println("Aucune tâche à venir trouvée pour l'utilisateur: " + currentUsername);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des grilles du dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configureOtherGrid() {
        setupGridCss(grid_task_missed);
        setupGridCss(grid_task_upcomming);
        grid_task_upcomming.setItems();
        setupOtherGridColumns(grid_task_missed);
        setupOtherGridColumns(grid_task_upcomming);
    }

    private void setupGridCss(Grid<Task> grid) {
        grid.addClassNames("task-grid");
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setColumnReorderingAllowed(true);
        grid.getStyle().remove("height");
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassName("no-scrollbar");
        grid.getStyle().set("--vaadin-grid_principal-cell-padding", "12px 8px");
    }

    private void setupOtherGridColumns(Grid<Task> grid) {
        grid.addColumn(Task::getLibelle)
                .setHeader("Libellé")
                .setResizable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(task -> {
                    TaskPriority taskPriority = task.getPriority();
                    Span priorityBadge = new Span(taskPriority.name());
                    String variant = getPriorityBadgeVariant(taskPriority);
                    priorityBadge.getElement().getThemeList().add("badge " + variant);
                    return priorityBadge;
                }))
                .setHeader("Priorité")
                .setWidth("120px")
                .setFlexGrow(0)
                .setResizable(true);
        grid.addColumn(task -> {
                    LocalDate date = task.getDateLimite();
                    return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                })
                .setHeader("Date limite")
                .setResizable(true)
                .setWidth("130px")
                .setFlexGrow(0);
    }
    private void onTaskCreated(Task savedTask) {
        updateDashboardGrids();
        updateKpiValues();
    }
    private void onFormClosed() {
        System.out.println("Formulaire de création de tâche fermé");
    }
    private String getPriorityBadgeVariant(TaskPriority priority) {
        return switch (priority) {
            case TRES_BASSE -> "success";
            case BASSE     -> "primary";
            case MOYENNE   -> "contrast";
            case HAUTE     -> "warning";
            case CRITIQUE  -> "error";
            default        -> "contrast";
        };
    }
}