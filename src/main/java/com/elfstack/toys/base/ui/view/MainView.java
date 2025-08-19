package com.elfstack.toys.base.ui.view;

import com.elfstack.toys.admin.service.CalendarService;
import com.elfstack.toys.admin.service.HolidaySyncService;

import com.elfstack.toys.security.dev.DevSecurityService;
import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.TaskPriority;
import com.elfstack.toys.taskmanagement.service.TaskService;
import com.elfstack.toys.usermanagement.service.KeycloakUserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
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

    private final Grid<Task> grid_task_missed = new Grid<>(Task.class, false);
    private final Grid<Task> grid_task_upcomming = new Grid<>(Task.class, false);
    private H4 missedTitle;
    private H4 upcomingTitle;
    private Card card_missing_task =  new Card();
    private Card card_upcoming_task =  new Card();

    public MainView(TaskService taskService,
                        KeycloakUserService keycloakUserService,
                        CalendarService calendarService,
                        HolidaySyncService holidaySyncService,
                    DevSecurityService devSecurityService) {
        this.taskService = taskService;
        this.devSecurityService = devSecurityService;

        addClassName("task-management-view");
        setSizeFull();
        setupUI();
        updateDashboardGrids();
    }

    private void setupUI() {
        configureOtherGrid();
        H3 title = new H3("Dashboard");
        HorizontalLayout header = new HorizontalLayout(title);
        header.setMargin(false);
        missedTitle = new H4("Tâches en retard (0)");
        missedTitle.getStyle().set("color", "#a51b1b").set("margin", "10px");

        upcomingTitle = new H4("À Faire dans la semaine (0)");
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
        mainLayout.setFlexGrow(1, cardLayout);

        mainLayout.add(header,cardLayout);

        add(mainLayout);

        addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Height.FULL,
                LumoUtility.Width.FULL
        );
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
            UI.getCurrent().access(this::updateDashboardTitles);
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
