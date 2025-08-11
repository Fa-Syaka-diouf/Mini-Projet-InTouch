package com.elfstack.toys.base.ui.view;

import com.elfstack.toys.admin.service.CalendarService;
import com.elfstack.toys.admin.service.HolidaySyncService;
import com.elfstack.toys.base.ui.component.ViewToolbar;

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
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route (value = "" , layout = MainLayout.class)
@RolesAllowed({"ADMIN", "USER"})
public final class MainView extends VerticalLayout {

    private final TaskService taskService;
    private final CalendarService calendarService;
    private final HolidaySyncService holidaySyncService;
    private final DevSecurityService devSecurityService;

    private final Grid<Task> grid = new Grid<>(Task.class, false);
    private final Button addTaskButton = new Button("Nouvelle tâche");
    private TextField searchField;
    private GridListDataView<Task> dataView;
    private Editor<Task> editor;

    private CheckboxGroup<String> columnVisibilityGroup;
    private Button button;
    private Popover popover;
    private final List<String> allColumns = Arrays.asList("statut","pays", "priority", "dateLimite", "sla", "description");
    private final Set<String> defaultVisibleColumns = new HashSet<>(Arrays.asList("statut", "dateLimite"));

    private final Dialog formDialog = new Dialog();
    private final FormLayout formLayout = new FormLayout();
    private final TextField libelle = new TextField("Libellé (*)");
    private final ComboBox<StatutEnum> statut = new ComboBox<>("Statut (*)");
    private final TextArea description = new TextArea("Description (*)");
    private final IntegerField slaDays = new IntegerField("SLA (jours) *");
    private final ComboBox<String> paysDestinataire = new ComboBox<>("Pays destinataire (*)");
    private final DatePicker dateLimite = new DatePicker("Date limite calculée");
    private final ComboBox<TaskPriority> priority = new ComboBox<>("Priorité (*)");
    private final Button saveButton = new Button("Enregistrer");
    private final Button cancelButton = new Button("Annuler");
    private final Button deleteButton = new Button("Supprimer");
    private Card card =  new Card();
    private final Dialog detailsDialog = new Dialog();

    private final Binder<Task> binder = new BeanValidationBinder<>(Task.class);
    private Task currentTask;

    public MainView(TaskService taskService,
                        KeycloakUserService keycloakUserService,
                        CalendarService calendarService,
                        HolidaySyncService holidaySyncService,
                    DevSecurityService devSecurityService) {
        this.taskService = taskService;
        this.devSecurityService = devSecurityService;
        this.calendarService = calendarService;
        this.holidaySyncService = holidaySyncService;

        addClassName("task-management-view");
        setSizeFull();

        setupUI();
        configureForm();
        configureDialog();
        configureDetailsDialog();

        updateList();
        loadInitialData();
        currentTask = null;
    }

    private void setupUI() {
        addTaskButton.getStyle().setBackgroundColor("#243163");
        addTaskButton.getStyle().setColor("white !important");
        addTaskButton.setIcon(VaadinIcon.PLUS.create());
        addTaskButton.addClickListener(e -> {
            this.currentTask = new Task();
            openForm(currentTask);
        });
        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        setupColumnVisibility();
        configureGrid();
        H3 title = new H3("Liste de vos Taches");
        HorizontalLayout header = new HorizontalLayout(title);
        HorizontalLayout toolbarLayout = new HorizontalLayout(searchField, addTaskButton, button);
        toolbarLayout.setAlignItems(HorizontalLayout.Alignment.END);
        toolbarLayout.setWidthFull();
        toolbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbarLayout.setFlexGrow(1, searchField);

        VerticalLayout layout = new VerticalLayout(toolbarLayout, grid);
        layout.setPadding(false);
        layout.setFlexGrow(1, grid);
        layout.setSizeFull();
        layout.setSpacing(true);
        card.removeAll();
        card.add(layout,popover);
        card.setSizeFull();
        card.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid rgba(0, 0, 0, 0.1)")
                .set("border-radius", "10px");
        this.removeAll();
        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.setFlexGrow(0, header);
        mainLayout.setFlexGrow(1, card);

        mainLayout.add(header, card);

        add(mainLayout);

        addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Height.FULL,
                LumoUtility.Width.FULL
        );
    }

    private void setupColumnVisibility() {
        button = new Button(VaadinIcon.GRID_H.create());
        button.addThemeVariants(ButtonVariant.LUMO_ICON);
        button.setAriaLabel("Show / hide columns");
        popover = new Popover();
        popover.setModal(true);
        popover.setBackdropVisible(true);
        popover.setPosition(PopoverPosition.BOTTOM_END);
        popover.setTarget(button);

        Div heading = new Div("Configure columns");
        heading.getStyle().set("font-weight", "600");
        heading.getStyle().set("padding", "var(--lumo-space-xs)");
        columnVisibilityGroup = new CheckboxGroup<>();
        columnVisibilityGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        columnVisibilityGroup.setItems(allColumns);
        columnVisibilityGroup.setValue(defaultVisibleColumns);

        columnVisibilityGroup.setItemLabelGenerator(item -> {
            return switch (item) {
                case "statut" -> "Statut";
                case "pays" -> "Pays";
                case "priority" -> "Priorité";
                case "dateLimite" -> "Date limite";
                case "sla" -> "SLA (jours)";
                case "description" -> "Description";
                default -> item;
            };
        });

        columnVisibilityGroup.addValueChangeListener(e -> {
            allColumns.forEach(key -> {
                Grid.Column<Task> column = grid.getColumnByKey(key);
                if (column != null) {
                    column.setVisible(e.getValue().contains(key));
                }
            });
        });
        Button showAll = new Button("Tous", (e) -> {
            columnVisibilityGroup.setValue(new HashSet<>(allColumns));
        });
        showAll.addThemeVariants(ButtonVariant.LUMO_SMALL);

        Button reset = new Button("Reset", (e) -> {
            columnVisibilityGroup.setValue(defaultVisibleColumns);
        });
        reset.addThemeVariants(ButtonVariant.LUMO_SMALL);

        HorizontalLayout footer = new HorizontalLayout(showAll, reset);
        footer.setSpacing(false);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        popover.add(heading, columnVisibilityGroup, footer);

    }

    private void configureGrid() {
        grid.addClassNames("task-grid");
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setColumnReorderingAllowed(true);
        grid.getStyle().remove("height");
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addClassName("no-scrollbar");
        grid.getStyle().set("--vaadin-grid-cell-padding", "12px 8px");



        editor = grid.getEditor();
        Binder<Task> gridBinder = new Binder<>(Task.class);
        editor.setBinder(gridBinder);
        editor.setBuffered(true);

        setupGridColumns(gridBinder);
        setupEditorEvents();


        dataView = grid.setItems();
        setupSearchFilter();
    }

    private void setupGridColumns(Binder<Task> gridBinder) {

        Grid.Column<Task> libelleColumn = grid.addColumn(Task::getLibelle)
                .setKey("libelle")
                .setHeader("Libellé")
                .setSortable(true)
                .setResizable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);
        TextField libelleField = new TextField();
        libelleField.setWidthFull();
        gridBinder.forField(libelleField)
                .asRequired("Le libellé ne peut pas être vide")
                .bind(Task::getLibelle, Task::setLibelle);
        libelleColumn.setEditorComponent(libelleField);


        Grid.Column<Task> statutColumn = grid.addColumn(new ComponentRenderer<>(task -> {
                    StatutEnum status = task.getStatut();
                    String label = status.name();
                    String badgeVariant = getStatusBadgeVariant(status);

                    Span badgeSpan = new Span(label);
                    badgeSpan.getElement().getThemeList().add("badge " + badgeVariant);
                    return badgeSpan;
                })).setKey("statut")
                .setHeader("Statut")
                .setSortable(true)
                .setWidth("120px")
                .setFlexGrow(0)
                .setResizable(true);
        ComboBox<StatutEnum> statutField = new ComboBox<>();
        statutField.setItems(StatutEnum.values());
        statutField.setItemLabelGenerator(StatutEnum::name);
        statutField.setWidthFull();
        gridBinder.forField(statutField)
                .asRequired("Le statut ne peut pas être vide")
                .bind(Task::getStatut, Task::setStatut);
        statutColumn.setEditorComponent(statutField);




        Grid.Column<Task> paysColumn = grid.addColumn(Task::getPaysDestinataire)
                .setKey("pays")
                .setHeader("Pays")
                .setSortable(true)
                .setResizable(true)
                .setWidth("100px")
                .setFlexGrow(0);
        ComboBox<String> paysField = new ComboBox<>();
        try {
            List<String> countries = holidaySyncService.getAllCountries();
            paysField.setItems(countries);
        } catch (Exception e) {
            paysField.setItems("FR", "UK", "US", "DE", "ES", "IT");
        }
        paysField.setWidthFull();
        gridBinder.forField(paysField)
                .asRequired("Le pays ne peut pas être vide")
                .bind(Task::getPaysDestinataire, Task::setPaysDestinataire);
        paysColumn.setEditorComponent(paysField);


        Grid.Column<Task> priorityColumn = grid.addColumn(new ComponentRenderer<>(task -> {
                    TaskPriority taskPriority = task.getPriority();
                    Span priorityBadge = new Span(taskPriority.name());
                    String variant = getPriorityBadgeVariant(taskPriority);
                    priorityBadge.getElement().getThemeList().add("badge " + variant);
                    return priorityBadge;
                })).setKey("priority")
                .setHeader("Priorité")
                .setSortable(true)
                .setWidth("120px")
                .setFlexGrow(0)
                .setResizable(true);
        ComboBox<TaskPriority> priorityField = new ComboBox<>();
        priorityField.setItems(TaskPriority.values());
        priorityField.setItemLabelGenerator(TaskPriority::name);
        priorityField.setWidthFull();
        gridBinder.forField(priorityField)
                .asRequired("La priorité ne peut pas être vide")
                .bind(Task::getPriority, Task::setPriority);
        priorityColumn.setEditorComponent(priorityField);


        Grid.Column<Task> dateLimiteColumn = grid.addColumn(task -> {
                    LocalDate date = task.getDateLimite();
                    return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                }).setKey("dateLimite")
                .setHeader("Date limite")
                .setSortable(true)
                .setResizable(true)
                .setWidth("130px")
                .setFlexGrow(0);


        Grid.Column<Task> slaColumn = grid.addColumn(Task::getSlaDays)
                .setKey("sla")
                .setHeader("SLA (jours)")
                .setSortable(true)
                .setResizable(true)
                .setWidth("110px")
                .setFlexGrow(0);
        IntegerField slaField = new IntegerField();
        slaField.setMin(1);
        slaField.setWidthFull();
        gridBinder.forField(slaField)
                .asRequired("Le SLA ne peut pas être vide")
                .withValidator(val -> val != null && val >= 1, "Le SLA doit être au moins 1")
                .withConverter(
                        integer -> integer != null ? Long.valueOf(integer) : null,
                        longValue -> longValue != null ? Math.toIntExact(longValue) : null
                )
                .bind(Task::getSlaDays, Task::setSlaDays);
        slaColumn.setEditorComponent(slaField);


        Grid.Column<Task> descriptionColumn = grid.addColumn(task -> {
                    String desc = task.getDescription();
                    return desc != null && desc.length() > 50 ? desc.substring(0, 50) + "..." : desc;
                }).setKey("description")
                .setHeader("Description")
                .setSortable(true)
                .setResizable(true)
                .setWidth("200px")
                .setFlexGrow(0);
        descriptionColumn.setVisible(false);


        Grid.Column<Task> actionsColumn = grid.addComponentColumn(task -> {
                    Button editButton = new Button("Éditer");
                    editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                    editButton.addClickListener(e -> {
                        if (editor.isOpen())
                            editor.cancel();
                        grid.getEditor().editItem(task);
                    });

                    Button detailsButton = new Button("Détails");
                    detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                    detailsButton.addClickListener(e -> showTaskDetails(task));

                    HorizontalLayout actionButtons = new HorizontalLayout(editButton, detailsButton);
                    actionButtons.setPadding(false);
                    actionButtons.setSpacing(false);
                    return actionButtons;
                }).setKey("actions")
                .setHeader("Actions")
                .setResizable(true)
                .setFrozenToEnd(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        Button saveGridButton = new Button("Sauvegarder", e -> {
            if (editor.save()) {
                showSuccessNotification("Tâche sauvegardée avec succès !");
            }
        });
        saveGridButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button cancelGridButton = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
        cancelGridButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        HorizontalLayout editorActions = new HorizontalLayout(saveGridButton, cancelGridButton);
        editorActions.setPadding(false);
        actionsColumn.setEditorComponent(editorActions);
    }

    private void configureDetailsDialog() {
        detailsDialog.setModal(true);
        detailsDialog.setDraggable(true);
        detailsDialog.setResizable(true);
        detailsDialog.setWidth("600px");
        detailsDialog.setHeight("500px");
    }

    private void showTaskDetails(Task task) {
        detailsDialog.removeAll();

        // Configuration du dialog pour une taille optimale
        detailsDialog.setWidth("800px");
        detailsDialog.setHeight("auto");
        detailsDialog.setMaxHeight("90vh");
        detailsDialog.setResizable(false);
        detailsDialog.setDraggable(true);

        // Header avec titre et statut
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.addClassNames(LumoUtility.Padding.Bottom.MEDIUM);
        header.getStyle()
                .set("border-bottom", "2px solid var(--lumo-contrast-10pct)")
                .set("margin-bottom", "var(--lumo-space-l)");

        H2 title = new H2("Détails de la tâche");
        title.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.XLARGE);
        title.getStyle().set("color", "var(--lumo-primary-color)");

        // Badge de statut
        Span statusBadge = createStatusBadge(task.getStatut());

        header.add(title, statusBadge);

        // Contenu principal en deux colonnes
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setWidthFull();
        mainContent.setSpacing(true);
        mainContent.addClassNames(LumoUtility.Gap.LARGE);

        // Colonne gauche - Informations principales
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);
        leftColumn.setWidth("50%");
        leftColumn.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM);
        leftColumn.getStyle().set("padding", "var(--lumo-space-m)");

        H4 leftTitle = new H4("Informations générales");
        leftTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL, LumoUtility.TextColor.PRIMARY);
        leftColumn.add(leftTitle);

        leftColumn.add(createStyledDetailRow("📋", "Libellé", task.getLibelle()));
        leftColumn.add(createStyledDetailRow("📝", "Description", task.getDescription()));
        leftColumn.add(createStyledDetailRow("⚡", "Priorité",
                task.getPriority() != null ? task.getPriority().name() : "N/A"));
        leftColumn.add(createStyledDetailRow("🌍", "Pays destinataire", task.getPaysDestinataire()));

        // Colonne droite - Dates et SLA
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);
        rightColumn.setWidth("50%");
        rightColumn.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM);
        rightColumn.getStyle().set("padding", "var(--lumo-space-m)");

        H4 rightTitle = new H4("Planification & Dates");
        rightTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL, LumoUtility.TextColor.PRIMARY);
        rightColumn.add(rightTitle);

        rightColumn.add(createStyledDetailRow("⏱️", "SLA (jours)",
                task.getSlaDays() != null ? task.getSlaDays().toString() + " jours" : "N/A"));
        rightColumn.add(createStyledDetailRow("📅", "Date de création",
                task.getCreationDate() != null ?
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(task.getCreationDate().atZone(ZoneId.systemDefault())) : "N/A"));
        rightColumn.add(createStyledDetailRow("⏰", "Date limite",
                task.getDateLimite() != null ?
                        task.getDateLimite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A"));
        rightColumn.add(createStyledDetailRow("✅", "Date de fin",
                task.getDateFin() != null ?
                        task.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "En cours"));

        mainContent.add(leftColumn, rightColumn);

        // Section des actions avec séparateur visuel
        Div separator = new Div();
        separator.addClassNames(LumoUtility.Background.CONTRAST_20);
        separator.getStyle()
                .set("height", "1px")
                .set("width", "100%")
                .set("margin", "var(--lumo-space-l) 0");

        // Boutons d'action avec icônes et styles améliorés
        Button deleteButton = new Button("Supprimer", VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        deleteButton.addClickListener(e -> {
            deleteTaskFromDetails(task);
            detailsDialog.close();
        });

        Button terminateButton = new Button("Terminer", VaadinIcon.CHECK_CIRCLE.create());
        terminateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        terminateButton.addClickListener(e -> {
            UpdateTaskStatus(task);
            detailsDialog.close();
        });

        if (task.getStatut().name().equals("COMPLETED") || task.getDateFin() != null) {
            terminateButton.setEnabled(false);
            terminateButton.setText("Déjà terminée");
        }

        Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        editButton.addClickListener(e -> {
            detailsDialog.close();
            openForm(task);
        });

        Button closeButton = new Button("Fermer", VaadinIcon.CLOSE.create());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(e -> detailsDialog.close());

        HorizontalLayout actionButtons = new HorizontalLayout(editButton, terminateButton);
        actionButtons.setSpacing(true);

        HorizontalLayout dangerButtons = new HorizontalLayout(deleteButton);

        HorizontalLayout navigationButtons = new HorizontalLayout(closeButton);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        buttonLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonLayout.add(dangerButtons, actionButtons, navigationButtons);
        buttonLayout.addClassNames(LumoUtility.Padding.Top.MEDIUM);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(false);
        dialogLayout.addClassNames(LumoUtility.Gap.MEDIUM);
        dialogLayout.getStyle()
                .set("max-height", "85vh")
                .set("overflow-y", "auto");

        dialogLayout.add(header, mainContent, separator, buttonLayout);

        detailsDialog.add(dialogLayout);
        detailsDialog.open();
    }

    private Span createStatusBadge(Object status) {
        Span badge = new Span(status.toString());
        badge.addClassNames(
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.XSMALL,
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.SEMIBOLD
        );

        // Couleurs selon le statut
        String statusName = status.toString().toUpperCase();
        switch (statusName) {
            case "COMPLETED":
            case "DONE":
                badge.addClassNames(LumoUtility.Background.SUCCESS_10, LumoUtility.TextColor.SUCCESS);
                break;
            case "IN_PROGRESS":
            case "PENDING":
                badge.addClassNames(LumoUtility.Background.PRIMARY_10, LumoUtility.TextColor.PRIMARY);
                break;
            case "CANCELLED":
            case "FAILED":
                badge.addClassNames(LumoUtility.Background.ERROR_10, LumoUtility.TextColor.ERROR);
                break;
            default:
                badge.addClassNames(LumoUtility.Background.CONTRAST_10, LumoUtility.TextColor.SECONDARY);
        }

        return badge;
    }

    private HorizontalLayout createStyledDetailRow(String icon, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.START);
        row.setSpacing(true);
        row.addClassNames(LumoUtility.Padding.Vertical.XSMALL);

        // Icône
        Span iconSpan = new Span(icon);
        iconSpan.getStyle()
                .set("font-size", "16px")
                .set("width", "20px")
                .set("text-align", "center");

        // Label
        Span labelSpan = new Span(label);
        labelSpan.addClassNames(
                LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.SMALL
        );
        labelSpan.setWidth("120px");
        labelSpan.getStyle().set("flex-shrink", "0");

        // Valeur avec gestion du texte long
        Span valueSpan = new Span(value != null && !value.isEmpty() ? value : "Non renseigné");
        valueSpan.addClassNames(LumoUtility.FontSize.SMALL);

        if (value == null || value.isEmpty() || value.equals("N/A")) {
            valueSpan.addClassNames(LumoUtility.TextColor.TERTIARY);
            valueSpan.getStyle().set("font-style", "italic");
        }

        // Gestion du texte long pour la description
        if (label.equals("Description") && value != null && value.length() > 100) {
            valueSpan.setText(value.substring(0, 100) + "...");
            valueSpan.getElement().setAttribute("title", value); // Tooltip avec texte complet
        }

        row.add(iconSpan, labelSpan, valueSpan);
        row.setFlexGrow(1, valueSpan);

        return row;
    }

    private Div createDetailRow(String label, String value) {
        Div row = new Div();
        row.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.MEDIUM, LumoUtility.Padding.Vertical.SMALL);

        Span labelSpan = new Span(label);
        labelSpan.addClassNames(LumoUtility.FontWeight.SEMIBOLD);
        labelSpan.setWidth("150px");

        Span valueSpan = new Span(value != null ? value : "N/A");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private void deleteTaskFromDetails(Task task) {
        if (task.getId() != null) {
            try {
                taskService.delete(task.getId());
                updateList();
                showSuccessNotification("Tâche supprimée avec succès !");
            } catch (Exception e) {
                showErrorNotification("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }
    private void UpdateTaskStatus(Task task) {
        if (task.getId() != null) {
            try {
                taskService.markAsCompleted(task.getId());
                updateList();
                showSuccessNotification("Tâche mise a jour avec succès !");
            } catch (Exception e) {
                showErrorNotification("Erreur lors de la mise a jour : " + e.getMessage());
            }
        }
    }

    private void setupEditorEvents() {
        editor.addSaveListener(event -> {
            try {
                Task task = event.getItem();
                Task savedTask = taskService.update(task);
                updateList();
                showSuccessNotification("Tâche mise à jour avec succès !");
            } catch (Exception e) {
                showErrorNotification("Erreur lors de la sauvegarde : " + e.getMessage());
                editor.cancel();
            }
        });
    }

    private void setupSearchFilter() {
        searchField.addValueChangeListener(e -> {
            if (dataView != null) {
                dataView.refreshAll();
            }
        });

        dataView.addFilter(task -> {
            String searchTerm = searchField.getValue();
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return true;
            }

            searchTerm = searchTerm.trim().toLowerCase();
            boolean matchesLibelle = task.getLibelle().toLowerCase().contains(searchTerm);
            boolean matchesDescription = task.getDescription().toLowerCase().contains(searchTerm);
            boolean matchesResponsable = task.getResponsableFullname().toLowerCase().contains(searchTerm);
            boolean matchesPays = task.getPaysDestinataire().toLowerCase().contains(searchTerm);

            return matchesLibelle || matchesDescription || matchesResponsable || matchesPays;
        });
    }

    private void configureForm() {
        configureFormFields();
        configureBinder();
        configureFormButtons();
        configureFormLayout();
    }

    private void configureFormFields() {
        libelle.setPlaceholder("Titre de la tâche");
        libelle.setRequired(true);

        statut.setItems(StatutEnum.values());
        statut.setItemLabelGenerator(StatutEnum::name);
        statut.setPlaceholder("Sélectionner un statut");
        statut.setValue(StatutEnum.A_FAIRE);
        statut.setRequired(true);

        description.setPlaceholder("Description détaillée de la tâche");
        description.setHeight("120px");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setRequired(true);

        slaDays.setMin(1);
        slaDays.setStep(1);
        slaDays.setPlaceholder("Nombre de jours pour le SLA");
        slaDays.setRequiredIndicatorVisible(true);

        paysDestinataire.setPlaceholder("Sélectionner un pays");
        paysDestinataire.setRequired(true);

        dateLimite.setPlaceholder("jj/mm/aaaa");
        dateLimite.setLocale(Locale.FRANCE);
        dateLimite.setReadOnly(true);

        priority.setItems(TaskPriority.values());
        priority.setItemLabelGenerator(TaskPriority::name);
        priority.setPlaceholder("Sélectionner une priorité");
        priority.setValue(TaskPriority.NORMALE);
        priority.setRequired(true);

        paysDestinataire.addValueChangeListener(e -> updateDueDate());
        slaDays.addValueChangeListener(e -> updateDueDate());
    }

    private void configureBinder() {
        binder.forField(libelle)
                .asRequired("Le libellé est obligatoire")
                .withValidator(t -> t != null && t.trim().length() >= 3, "Le libellé doit contenir au moins 3 caractères")
                .bind(Task::getLibelle, Task::setLibelle);

        binder.forField(statut)
                .asRequired("Le statut est obligatoire")
                .bind(Task::getStatut, Task::setStatut);

        binder.forField(description)
                .asRequired("La description est obligatoire")
                .withValidator(d -> d != null && !d.trim().isEmpty(), "La description ne peut pas être vide")
                .bind(Task::getDescription, Task::setDescription);

        binder.forField(paysDestinataire)
                .asRequired("Le pays est obligatoire")
                .bind(Task::getPaysDestinataire, Task::setPaysDestinataire);

        binder.forField(dateLimite)
                .bind(Task::getDateLimite, Task::setDateLimite);

        binder.forField(priority)
                .asRequired("La priorité est obligatoire")
                .bind(Task::getPriority, Task::setPriority);

        binder.forField(slaDays)
                .asRequired("Le SLA est obligatoire")
                .withValidator(val -> val != null && val >= 1, "Le SLA doit être au moins 1")
                .withConverter(
                        integer -> integer != null ? Long.valueOf(integer) : null,
                        longValue -> longValue != null ? Math.toIntExact(longValue) : null
                )
                .bind(Task::getSlaDays, Task::setSlaDays);

    }

    private void configureFormButtons() {
        saveButton.getStyle().setBackgroundColor("#243163");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        saveButton.getStyle().set("margin-right", "var(--lumo-space-s)");
        deleteButton.getStyle().set("margin-left", "auto");

        saveButton.addClickListener(e -> saveTask());
        cancelButton.addClickListener(e -> closeForm());
        deleteButton.addClickListener(e -> deleteTask());
    }

    private void configureFormLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton, deleteButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.START);
        buttonLayout.setWidthFull();

        formLayout.add(
                libelle,
                statut,
                description,
                paysDestinataire,
                slaDays,
                dateLimite,
                priority,
                buttonLayout
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        formLayout.setColspan(description, 2);
        formLayout.setColspan(buttonLayout, 2);
    }

    private void configureDialog() {
        formDialog.add(formLayout);
        formDialog.setModal(true);
        formDialog.setDraggable(true);
        formDialog.setResizable(true);
        formDialog.setWidth("800px");
        formDialog.setHeight("600px");
    }

    private void loadInitialData() {
        loadAvailableCountries();
    }

    private void loadAvailableCountries() {
        try {
            List<String> countries = holidaySyncService.getAllCountries();
            System.out.println(countries);
            if (countries != null && !countries.isEmpty()) {
                paysDestinataire.setItems(countries);
            } else {
                paysDestinataire.setItems("FR", "UK", "US", "DE", "ES", "IT");
                paysDestinataire.setValue("FR");
            }
        } catch (Exception e) {
            paysDestinataire.setItems("FR", "UK", "US", "DE", "ES", "IT");
            paysDestinataire.setValue("FR");
        }
    }

    private void openForm(Task task) {
        this.currentTask = task;

        if (task.getId() == null) {
            task.setCreationDate(Instant.now());
            task.setStatut(StatutEnum.A_FAIRE);
            task.setPriority(TaskPriority.NORMALE);
            deleteButton.setVisible(false);
        } else {
            deleteButton.setVisible(true);
        }

        binder.setBean(task);
        updateDueDate();

        formDialog.setHeaderTitle(task.getId() == null ? "Nouvelle tâche" : "Modifier la tâche : " + task.getLibelle());
        formDialog.open();
    }

    private void closeForm() {
        formDialog.close();
        currentTask = null;
        binder.setBean(null);
    }

    private void saveTask() {
        try {
            if (binder.writeBeanIfValid(currentTask)) {
                boolean isNew = currentTask.getId() == null;
                if (isNew){
                    currentTask.setResponsableUsername(devSecurityService.getCurrentUsername());
                    Task savedTask = taskService.save(currentTask);
                }
                else{
                    Task savedTask = taskService.update(currentTask);
                }

                updateList();
                closeForm();

                String message = isNew ? "Tâche créée avec succès !" : "Tâche mise à jour avec succès !";
                Notification.show(message)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Veuillez corriger les erreurs dans le formulaire")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            Notification.show("Erreur lors de la sauvegarde : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteTask() {
        if (currentTask.getId() != null) {
            try {
                taskService.delete(currentTask.getId());
                updateList();
                closeForm();
                Notification.show("Tâche supprimée avec succès !")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception e) {
                Notification.show("Erreur lors de la suppression : " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }

    private void updateDueDate() {
        try {
            if (currentTask == null || slaDays.getValue() == null) {
                dateLimite.clear();
                return;
            }

            LocalDate startDate = currentTask.getCreationDate()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String countryCode = holidaySyncService.countryCodeSetup(paysDestinataire.getValue());
            LocalDate computedDueDate = calendarService.calculateDueDate(
                    startDate,
                    slaDays.getValue(),
                    countryCode
            );

            dateLimite.setValue(computedDueDate);
            currentTask.setDateLimite(computedDueDate);

        } catch (Exception e) {
            Notification.show("Erreur lors du calcul de la date limite : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            dateLimite.clear();
            if (currentTask != null) currentTask.setDateLimite(null);
        }
    }

    private void updateList() {
        try {
            String CurrentUsername = devSecurityService.getCurrentUsername(); // Récupération de l'utilisateur connecté
            List<Task> tasks = taskService.findAll()
                    .stream()
                    .filter(task -> CurrentUsername.equals(task.getResponsableUsername()))
                    .toList();
            if (dataView != null) {
                dataView = grid.setItems(tasks);
                setupSearchFilter();
            } else {
                grid.setItems(tasks);
            }
        } catch (Exception e) {
            Notification.show("Erreur lors du chargement des tâches : " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String getStatusBadgeVariant(StatutEnum status) {
        return switch (status) {
            case A_FAIRE -> "warning";
            case EN_COURS -> "primary";
            case TERMINER -> "success";
            case SUSPENDU -> "error";
            default -> "contrast";
        };
    }

    private String getPriorityBadgeVariant(TaskPriority priority) {
        return switch (priority) {
            case FAIBLE -> "success";
            case NORMALE -> "primary";
            case ELEVEE -> "warning";
            case CRITIQUE -> "error";
            default -> "contrast";
        };
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
