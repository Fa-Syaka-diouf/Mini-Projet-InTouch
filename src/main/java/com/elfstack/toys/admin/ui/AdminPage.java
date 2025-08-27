package com.elfstack.toys.admin.ui;

import com.elfstack.toys.taskmanagement.domain.StatutEnum;
import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.TaskPriority;
import com.elfstack.toys.taskmanagement.service.TaskService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "admin", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
@PageTitle("Dashboard - Administration")
@Menu(order = 0, title = "Dashboard")
public final class AdminPage extends Main {

    private final TaskService taskService;
    private List<Task> allTasks;
    private ListDataProvider<Task> overdueTasksProvider;

    private DatePicker startDateFilter;
    private DatePicker endDateFilter;
    private ComboBox<String> countryFilter;
    private ComboBox<StatutEnum> statusFilter;
    private TextField responsibleFilter;

    private VerticalLayout kpiCardsLayout;
    private VerticalLayout chartsLayout;
    private Grid<Task> overdueTasksGrid;

    @Autowired
    public AdminPage(TaskService taskService) {
        this.taskService = taskService;
        addClassName(LumoUtility.Padding.MEDIUM);

        initializeData();
        createUI();
        refreshDashboard();
    }

    private void initializeData() {
        allTasks = taskService.findAll();
        overdueTasksProvider = new ListDataProvider<>(getOverdueTasks());
    }

    private void createUI() {
        H1 title = new H1("Dashboard - Statistiques des Tâches");
        title.addClassNames(LumoUtility.Margin.Bottom.LARGE);
        title.getStyle().setColor("#243163");
        add(title);
        add(createFiltersSection());

        kpiCardsLayout = new VerticalLayout();
        kpiCardsLayout.setSpacing(true);
        add(kpiCardsLayout);

        chartsLayout = createChartsSection();
        add(chartsLayout);

        add(createOverdueTasksSection());
    }

    private Component createFiltersSection() {

        startDateFilter = new DatePicker("Date de début");
        startDateFilter.setValue(LocalDate.now().minusMonths(1));
        startDateFilter.addValueChangeListener(e -> applyFilters());

        endDateFilter = new DatePicker("Date de fin");
        endDateFilter.setValue(LocalDate.now());
        endDateFilter.addValueChangeListener(e -> applyFilters());

        Set<String> countries = allTasks.stream()
                .map(Task::getPaysDestinataire)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        countryFilter = new ComboBox<>("Pays");
        countryFilter.setItems(countries);
        countryFilter.setClearButtonVisible(true);
        countryFilter.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(StatutEnum.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> applyFilters());

        responsibleFilter = new TextField("Responsable");
        responsibleFilter.setClearButtonVisible(true);
        responsibleFilter.addValueChangeListener(e -> applyFilters());

        Button resetFilters = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
        resetFilters.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        resetFilters.addClickListener(e -> resetFilters());

        FlexLayout filtersLayout = new FlexLayout(startDateFilter, endDateFilter, countryFilter, statusFilter, responsibleFilter, resetFilters);

        filtersLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        filtersLayout.setJustifyContentMode(FlexLayout.JustifyContentMode.START);
        filtersLayout.setAlignItems(FlexLayout.Alignment.END);

        startDateFilter.setMinWidth("200px");
        endDateFilter.setMinWidth("200px");
        countryFilter.setMinWidth("150px");
        statusFilter.setMinWidth("150px");
        responsibleFilter.setMinWidth("200px");
        resetFilters.setMinWidth("120px");

        filtersLayout.addClassName(LumoUtility.Gap.MEDIUM);
        filtersLayout.addClassName(LumoUtility.Padding.MEDIUM);
        filtersLayout.addClassName(LumoUtility.Background.CONTRAST_5);
        filtersLayout.addClassName(LumoUtility.BorderRadius.MEDIUM);

        return filtersLayout;
    }

    private void resetFilters() {
        startDateFilter.setValue(LocalDate.now().minusMonths(1));
        endDateFilter.setValue(LocalDate.now());
        countryFilter.clear();
        statusFilter.clear();
        responsibleFilter.clear();
        applyFilters();
    }

    private void applyFilters() {
        List<Task> filteredTasks = allTasks.stream()
                .filter(this::matchesFilters)
                .collect(Collectors.toList());

        refreshDashboardWithTasks(filteredTasks);
        overdueTasksProvider.getItems().clear();
        overdueTasksProvider.getItems().addAll(getOverdueTasksFromList(filteredTasks));
        overdueTasksProvider.refreshAll();
    }

    private boolean matchesFilters(Task task) {
        LocalDate taskDate = task.getCreationDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        if (startDateFilter.getValue() != null && taskDate.isBefore(startDateFilter.getValue())) {
            return false;
        }
        if (endDateFilter.getValue() != null && taskDate.isAfter(endDateFilter.getValue())) {
            return false;
        }
        if (countryFilter.getValue() != null && !countryFilter.getValue().equals(task.getPaysDestinataire())) {
            return false;
        }
        if (statusFilter.getValue() != null && !statusFilter.getValue().equals(task.getStatut())) {
            return false;
        }
        if (responsibleFilter.getValue() != null && !responsibleFilter.getValue().isEmpty()) {
            String filterValue = responsibleFilter.getValue().toLowerCase();
            String responsibleName = task.getResponsableFullname();
            if (responsibleName == null || !responsibleName.toLowerCase().contains(filterValue)) {
                return false;
            }
        }

        return true;
    }

    private VerticalLayout createChartsSection() {
        H3 chartsTitle = new H3("Graphiques");
        chartsTitle.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        // Placeholder pour les graphiques
        Div chartPlaceholder1 = new Div();
        chartPlaceholder1.setText("Graphique par statut (À implémenter avec Chart.js ou ApexCharts)");
        chartPlaceholder1.addClassName(LumoUtility.Padding.LARGE);
        chartPlaceholder1.addClassName(LumoUtility.Background.CONTRAST_5);
        chartPlaceholder1.addClassName(LumoUtility.BorderRadius.MEDIUM);
        chartPlaceholder1.setHeight("300px");
        chartPlaceholder1.setWidth("100%");

        Div chartPlaceholder2 = new Div();
        chartPlaceholder2.setText("Graphique par pays (À implémenter)");
        chartPlaceholder2.addClassName(LumoUtility.Padding.LARGE);
        chartPlaceholder2.addClassName(LumoUtility.Background.CONTRAST_5);
        chartPlaceholder2.addClassName(LumoUtility.BorderRadius.MEDIUM);
        chartPlaceholder2.setHeight("300px");
        chartPlaceholder2.setWidth("100%");

        HorizontalLayout chartsRow = new HorizontalLayout(chartPlaceholder1, chartPlaceholder2);
        chartsRow.setSizeFull();

        VerticalLayout section = new VerticalLayout(chartsTitle, chartsRow);
        section.addClassName(LumoUtility.Padding.MEDIUM);
        return section;
    }

    private Component createOverdueTasksSection() {
        H3 overdueTitle = new H3("Tâches en Retard");
        overdueTitle.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        overdueTasksGrid = new Grid<>(Task.class, false);
        overdueTasksGrid.setHeight("400px");
        overdueTasksGrid.setSizeFull();
        overdueTasksGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        overdueTasksGrid.setColumnReorderingAllowed(true);
        overdueTasksGrid.getStyle().remove("height");
        overdueTasksGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        overdueTasksGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        overdueTasksGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        overdueTasksGrid.addClassName("no-scrollbar");
        overdueTasksGrid.getStyle().set("--vaadin-grid-cell-padding", "12px 8px");

        // Configuration des colonnes
        overdueTasksGrid.addColumn(Task::getLibelle)
                .setHeader("Libellé")
                .setAutoWidth(true)
                .setFlexGrow(1);

        overdueTasksGrid.addColumn(Task::getResponsableFullname)
                .setHeader("Responsable")
                .setAutoWidth(true);

        overdueTasksGrid.addColumn(Task::getPaysDestinataire)
                .setHeader("Pays")
                .setAutoWidth(true);

        overdueTasksGrid.addColumn(task -> task.getSlaDays() + " jours")
                .setHeader("SLA")
                .setAutoWidth(true);

        overdueTasksGrid.addColumn(task ->
                        task.getDateLimite() != null ?
                                task.getDateLimite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) :
                                "Non définie"
                )
                .setHeader("Date Limite")
                .setAutoWidth(true);

        overdueTasksGrid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
                .setHeader("Statut")
                .setAutoWidth(true);

        overdueTasksGrid.addColumn(new ComponentRenderer<>(this::createPriorityBadge))
                .setHeader("Priorité")
                .setAutoWidth(true);

        overdueTasksGrid.setDataProvider(overdueTasksProvider);

        VerticalLayout section = new VerticalLayout(overdueTitle, overdueTasksGrid);
        section.addClassName(LumoUtility.Padding.MEDIUM);
        return section;
    }

    private Component createStatusBadge(Task task) {
        Span badge = new Span(task.getStatut().getValue());

        switch (task.getStatut()) {
            case A_FAIRE:
                badge.getElement().getThemeList().add("badge contrast");
                break;
            case EN_COURS:
                badge.getElement().getThemeList().add("badge");
                badge.addClassName("badge-custom-en-cours");
                break;
            case TERMINER:
                badge.getElement().getThemeList().add("badge success");
                break;
            case SUSPENDU:
                badge.getElement().getThemeList().add("badge error");
                break;
        }

        return badge;
    }

    private Component createPriorityBadge(Task task) {
        if (task.getPriority() == null) {
            return new Span("Normale");
        }

        Span badge = new Span(task.getPriority().getDisplayName());

        switch (task.getPriority()) {
            case TRES_BASSE:
                badge.getElement().getThemeList().add("badge contrast");
                break;
            case BASSE:
                badge.getElement().getThemeList().add("badge success");
                break;
            case MOYENNE:
                badge.getElement().getThemeList().add("badge");
                badge.addClassName("badge-custom-en-cours");
                break;
            case HAUTE:
                badge.getElement().getThemeList().add("badge warning");
                break;
            case CRITIQUE:
                badge.getElement().getThemeList().add("badge error");
                break;
        }

        return badge;
    }

    private void refreshDashboard() {
        refreshDashboardWithTasks(allTasks);
    }

    private void refreshDashboardWithTasks(List<Task> tasks) {
        kpiCardsLayout.removeAll();

        TaskStatistics stats = calculateStatistics(tasks);

        HorizontalLayout firstRow = new HorizontalLayout();
        firstRow.setWidthFull();
        firstRow.setSpacing(true);
        firstRow.setMargin(true);
        firstRow.getStyle().set("margin-bottom", "20px");
        firstRow.add(
                createKpiCard("Nombre Total", String.valueOf(stats.totalTasks),
                        VaadinIcon.TASKS.create(), "#243163","#f8f9fa"),
                createKpiCard("En Retard", String.valueOf(stats.overdueTasks),
                        VaadinIcon.WARNING.create(), "#a51b1b","#ffebee"),
                createKpiCard("Terminées", String.valueOf(stats.completedTasks),
                        VaadinIcon.CHECK_CIRCLE.create(), "#28a745","#e8f5e8"),
                createKpiCard("SLA Moyen", stats.averageSlaRespected + "%",
                        VaadinIcon.TIMER.create(), "#243163","#e3f2fd")
        );

        HorizontalLayout secondRow = new HorizontalLayout();
        secondRow.setWidthFull();
        secondRow.setSpacing(true);
        secondRow.setMargin(true);
        secondRow.getStyle().set("margin-bottom", "20px");
        secondRow.add(
                createKpiCard("À Faire", String.valueOf(stats.todoTasks),
                        VaadinIcon.CIRCLE.create(),"#243163","#f8f9fa"),
                createKpiCard("En Cours", String.valueOf(stats.inProgressTasks),
                        VaadinIcon.PLAY_CIRCLE.create(), "#28a745","#e8f5e8"),
                createKpiCard("Annulées", String.valueOf(stats.cancelledTasks),
                        VaadinIcon.CLOSE_CIRCLE.create(), "#a51b1b","#ffebee"),
                createKpiCard("Pays Actifs", String.valueOf(stats.activeCountries),
                        VaadinIcon.GLOBE.create(), "#243163","#e3f2fd")
        );

        kpiCardsLayout.add(firstRow, secondRow);
        kpiCardsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    }

    private Component createKpiCard(String title, String value, Icon icon, String iconColor, String backgroundColor) {
        Card card = new Card();
        card.addClassName(LumoUtility.Background.BASE);
        card.addClassName(LumoUtility.BorderRadius.MEDIUM);
        card.addClassName(LumoUtility.Padding.MEDIUM);
        card.addClassName(LumoUtility.BoxShadow.SMALL);
        card.setWidthFull();

        icon.setColor(iconColor);
        icon.setSize("24px");

        H2 valueText = new H2(value);
        valueText.getStyle()
                .set("color", iconColor)
                .set("margin", "0")
                .set("font-weight", "bold");;

        Span titleText = new Span(title);
        titleText.getStyle()
                .set("font-size", "14px")
                .set("color", "#666")
                .set("margin-bottom", "5px");

        HorizontalLayout header = new HorizontalLayout(icon, titleText);

        VerticalLayout content = new VerticalLayout(header, valueText);
        content.setSpacing(false);
        content.setPadding(false);

        card.add(content);
        setupKpiCardStyle(card, backgroundColor);
        return card;
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

        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("box-shadow", "0 4px 8px rgba(0,0,0,0.15)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        });
    }
    private TaskStatistics calculateStatistics(List<Task> tasks) {
        TaskStatistics stats = new TaskStatistics();
        stats.totalTasks = tasks.size();

        stats.todoTasks = (int) tasks.stream().filter(t -> t.getStatut() == StatutEnum.A_FAIRE).count();
        stats.inProgressTasks = (int) tasks.stream().filter(t -> t.getStatut() == StatutEnum.EN_COURS).count();
        stats.completedTasks = (int) tasks.stream().filter(t -> t.getStatut() == StatutEnum.TERMINER).count();
        stats.cancelledTasks = (int) tasks.stream().filter(t -> t.getStatut() == StatutEnum.SUSPENDU).count();

        stats.overdueTasks = getOverdueTasksFromList(tasks).size();

        stats.activeCountries = (int) tasks.stream()
                .map(Task::getPaysDestinataire)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long completedWithSla = tasks.stream()
                .filter(t -> t.getStatut() == StatutEnum.TERMINER && t.getSlaDays() != null)
                .count();
        stats.averageSlaRespected = completedWithSla > 0 ?
                (int) (completedWithSla * 100 / Math.max(1, stats.completedTasks)) : 0;

        return stats;
    }

    private List<Task> getOverdueTasks() {
        return getOverdueTasksFromList(allTasks);
    }

    private List<Task> getOverdueTasksFromList(List<Task> tasks) {
        LocalDate today = LocalDate.now();
        return tasks.stream()
                .filter(task -> task.getDateLimite() != null &&
                        task.getDateLimite().isBefore(today) &&
                        task.getStatut() != StatutEnum.TERMINER &&
                        task.getStatut() != StatutEnum.SUSPENDU)
                .collect(Collectors.toList());
    }

    private String getStatusLabel(StatutEnum statut) {
        switch (statut) {
            case A_FAIRE: return "À faire";
            case EN_COURS: return "En cours";
            case TERMINER: return "Terminé";
            case SUSPENDU: return "Annulé";
            default: return statut.name();
        }
    }


    private static class TaskStatistics {
        int totalTasks;
        int todoTasks;
        int inProgressTasks;
        int completedTasks;
        int cancelledTasks;
        int overdueTasks;
        int activeCountries;
        int averageSlaRespected;
    }
}