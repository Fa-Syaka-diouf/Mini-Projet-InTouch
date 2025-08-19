package com.elfstack.toys.admin.ui;

import com.elfstack.toys.taskmanagement.domain.StatutEnum;
import com.elfstack.toys.taskmanagement.domain.Task;
import com.elfstack.toys.taskmanagement.domain.TaskPriority;
import com.elfstack.toys.taskmanagement.service.TaskService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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

    // Composants de filtrage
    private DatePicker startDateFilter;
    private DatePicker endDateFilter;
    private ComboBox<String> countryFilter;
    private ComboBox<StatutEnum> statusFilter;
    private TextField responsibleFilter;

    // Composants d'affichage
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
        kpiCardsLayout.setPadding(false);
        kpiCardsLayout.addClassName(LumoUtility.Margin.MEDIUM);
        add(kpiCardsLayout);

        chartsLayout = createChartsSection();
        add(chartsLayout);

        add(createOverdueTasksSection());
    }

    private Component createFiltersSection() {
        H3 filtersTitle = new H3("Filtres");
        filtersTitle.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

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

        HorizontalLayout filterTopLayout = new HorizontalLayout(startDateFilter, endDateFilter, countryFilter);
        filterTopLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        filterTopLayout.addClassName(LumoUtility.Gap.MEDIUM);
        HorizontalLayout filterBottomLayout = new HorizontalLayout(statusFilter, responsibleFilter, resetFilters);
        filterBottomLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        filterBottomLayout.addClassName(LumoUtility.Gap.MEDIUM);
        HorizontalLayout filtersLayout = new HorizontalLayout(
                filterTopLayout, filterBottomLayout

        );
        filtersLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        filtersLayout.addClassName(LumoUtility.Gap.MEDIUM);

        VerticalLayout section = new VerticalLayout(filtersTitle, filtersLayout);
        section.addClassName(LumoUtility.Padding.MEDIUM);
        section.addClassName(LumoUtility.Background.CONTRAST_5);
        section.addClassName(LumoUtility.BorderRadius.MEDIUM);
        return section;
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

        Div chartPlaceholder2 = new Div();
        chartPlaceholder2.setText("Graphique par pays (À implémenter)");
        chartPlaceholder2.addClassName(LumoUtility.Padding.LARGE);
        chartPlaceholder2.addClassName(LumoUtility.Background.CONTRAST_5);
        chartPlaceholder2.addClassName(LumoUtility.BorderRadius.MEDIUM);
        chartPlaceholder2.setHeight("300px");

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

        // Calcul des statistiques
        TaskStatistics stats = calculateStatistics(tasks);

        // Création des cartes KPI
        HorizontalLayout firstRow = new HorizontalLayout();
        firstRow.setWidthFull();
        firstRow.addClassNames(LumoUtility.Margin.Bottom.LARGE);
        firstRow.add(
                createKpiCard("Nombre Total", String.valueOf(stats.totalTasks),
                        VaadinIcon.TASKS.create(), "var(--lumo-primary-color)"),
                createKpiCard("En Retard", String.valueOf(stats.overdueTasks),
                        VaadinIcon.WARNING.create(), "var(--lumo-error-color)"),
                createKpiCard("Terminées", String.valueOf(stats.completedTasks),
                        VaadinIcon.CHECK_CIRCLE.create(), "var(--lumo-success-color)"),
                createKpiCard("SLA Moyen", stats.averageSlaRespected + "%",
                        VaadinIcon.TIMER.create(), "var(--lumo-contrast-color)")
        );

        HorizontalLayout secondRow = new HorizontalLayout();
        secondRow.setWidthFull();
        secondRow.add(
                createKpiCard("À Faire", String.valueOf(stats.todoTasks),
                        VaadinIcon.CIRCLE.create(), "var(--lumo-contrast-color)"),
                createKpiCard("En Cours", String.valueOf(stats.inProgressTasks),
                        VaadinIcon.PLAY_CIRCLE.create(), "var(--lumo-primary-color)"),
                createKpiCard("Annulées", String.valueOf(stats.cancelledTasks),
                        VaadinIcon.CLOSE_CIRCLE.create(), "var(--lumo-error-color)"),
                createKpiCard("Pays Actifs", String.valueOf(stats.activeCountries),
                        VaadinIcon.GLOBE.create(), "var(--lumo-contrast-color)")
        );

        kpiCardsLayout.add(firstRow, secondRow);
        kpiCardsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    }

    private Component createKpiCard(String title, String value, Icon icon, String iconColor) {
        Div card = new Div();
        card.addClassName(LumoUtility.Background.BASE);
        card.addClassName(LumoUtility.BorderRadius.MEDIUM);
        card.addClassName(LumoUtility.Padding.MEDIUM);
        card.addClassName(LumoUtility.BoxShadow.SMALL);
        card.setWidthFull();

        icon.setColor(iconColor);
        icon.setSize("24px");

        H2 valueText = new H2(value);
        valueText.addClassName(LumoUtility.Margin.NONE);
        valueText.addClassName(LumoUtility.TextColor.PRIMARY);

        Span titleText = new Span(title);
        titleText.addClassName(LumoUtility.TextColor.SECONDARY);
        titleText.addClassName(LumoUtility.FontSize.SMALL);

        HorizontalLayout header = new HorizontalLayout(icon, titleText);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setWidthFull();

        VerticalLayout content = new VerticalLayout(header, valueText);
        content.setSpacing(false);
        content.setPadding(false);

        card.add(content);
        return card;
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

        // Calcul SLA moyen (simplifié)
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