//package com.elfstack.toys.admin.ui;
//
//import com.elfstack.toys.admin.domain.JourFerie;
//import com.elfstack.toys.admin.service.HolidaySyncService;
//import com.elfstack.toys.base.ui.component.ViewToolbar;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.button.ButtonVariant;
//import com.vaadin.flow.component.combobox.ComboBox;
//import com.vaadin.flow.component.datepicker.DatePicker;
//import com.vaadin.flow.component.dialog.Dialog;
//import com.vaadin.flow.component.formlayout.FormLayout;
//import com.vaadin.flow.component.grid.Grid;
//import com.vaadin.flow.component.grid.GridVariant;
//import com.vaadin.flow.component.grid.dataview.GridListDataView;
//import com.vaadin.flow.component.grid.editor.Editor;
//import com.vaadin.flow.component.html.H1;
//import com.vaadin.flow.component.html.Main;
//import com.vaadin.flow.component.icon.Icon;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.textfield.TextField;
//import com.vaadin.flow.data.binder.Binder;
//import com.vaadin.flow.data.binder.ValidationException;
//import com.vaadin.flow.data.provider.ListDataProvider;
//import com.vaadin.flow.data.value.ValueChangeMode;
//import com.vaadin.flow.router.Menu;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//import com.vaadin.flow.theme.lumo.LumoUtility;
//import jakarta.annotation.security.PermitAll;
//import jakarta.annotation.security.RolesAllowed;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Route(value = "admin/JourFerie", layout = AdminLayout.class)
//@PageTitle("Admin Page")
//@RolesAllowed("ADMIN")
//@Menu(order = 1, title = "Jours Fériés")
//public class AdminJourFerie extends Main {
//
//    private final HolidaySyncService holidaySyncService;
//    private Grid<JourFerie> grid;
//    private Editor<JourFerie> editor;
//    private final List<String> allCountries;
//    private final List<JourFerie> allHolidays;
//    private String currentSelectedCountry;
//    private GridListDataView<JourFerie> dataView;
//    private TextField searchField;
//    private Dialog createHolidayDialog;
//    private TextField dialogNomField;
//    private DatePicker dialogDateDebutField;
//    private DatePicker dialogDateFinField;
//    private TextField dialogPaysField;
//    private TextField dialogPaysCodeField;
//    private Binder<JourFerie> dialogBinder;
//
//    public AdminJourFerie(HolidaySyncService holidaySyncService) {
//        this.holidaySyncService = holidaySyncService;
//        allCountries = holidaySyncService.getAllCountries();
//        allHolidays = holidaySyncService.getAllJourFerie();
//
//        setupUI();
//        // Charger tous les jours fériés par défaut au démarrage
//        loadAllHolidays();
//    }
//
//    private void setupUI() {
//        setSizeFull();
//        ComboBox<String> countryComboBox = new ComboBox<>("Sélectionner un pays");
//        countryComboBox.setItems(allCountries);
//        countryComboBox.setPlaceholder("Choisir un pays...");
//        countryComboBox.addValueChangeListener(event -> {
//            String selectedCountry = event.getValue();
//            if (selectedCountry != null) {
//                currentSelectedCountry = selectedCountry;
//                loadHolidaysForCountry(selectedCountry);
//            } else {
//                currentSelectedCountry = null;
//                loadAllHolidays();
//            }
//        });
//
//        Button showAllButton = new Button("Afficher tous les jours fériés");
//        showAllButton.addClickListener(e -> {
//            countryComboBox.clear();
//            currentSelectedCountry = null;
//            loadAllHolidays();
//        });
//        showAllButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
//        Button createHolidayButton = new Button("Créer un jour férié");
//        createHolidayButton.addClickListener(e -> openCreateHolidayDialog());
//        createHolidayButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        createHolidayButton.setIcon(VaadinIcon.PLUS.create());
//        grid = new Grid<>(JourFerie.class, false);
//        editor = grid.getEditor();
//
//        Binder<JourFerie> binder = new Binder<>(JourFerie.class);
//        editor.setBinder(binder);
//        editor.setBuffered(true);
//        setupGridColumns(binder);
//        setupEditorEvents();
//
//        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
//        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX,
//                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
//
//        dataView = grid.setItems(new ArrayList<>());
//
//        searchField = new TextField();
//        searchField.setWidth("50%");
//        searchField.setPlaceholder("Search");
//        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
//        searchField.setValueChangeMode(ValueChangeMode.EAGER);
//
//        setupSearchFilter();
//
//        HorizontalLayout toolbarLayout = new HorizontalLayout(searchField, countryComboBox, showAllButton,createHolidayButton);
//        toolbarLayout.setAlignItems(HorizontalLayout.Alignment.END);
//        toolbarLayout.setWidthFull();
//        VerticalLayout layout = new VerticalLayout(toolbarLayout, grid);
//        layout.setPadding(false);
//
//        add(layout);
//    }
//    private void openCreateHolidayDialog() {
//        if (createHolidayDialog == null) {
//            createHolidayDialog = createHolidayDialog();
//        }
//        clearDialogForm();
//        createHolidayDialog.open();
//    }
//    private Dialog createHolidayDialog() {
//        Dialog dialog = new Dialog();
//        dialog.setHeaderTitle("Créer un nouveau jour férié");
//        dialog.setWidth("500px");
//        dialog.setHeight("400px");
//
//        FormLayout formLayout = new FormLayout();
//
//        dialogNomField = new TextField("Nom du jour férié");
//        dialogNomField.setWidthFull();
//        dialogNomField.setRequired(true);
//
//        dialogDateDebutField = new DatePicker("Date de début");
//        dialogDateDebutField.setWidthFull();
//        dialogDateDebutField.setRequired(true);
//
//        dialogDateFinField = new DatePicker("Date de fin");
//        dialogDateFinField.setWidthFull();
//        dialogDateFinField.setRequired(true);
//
//        ComboBox<String> dialogPaysField = new ComboBox<>("Sélectionner un pays");
//        dialogPaysField.setWidthFull();
//        dialogPaysField.setRequired(true);
//        dialogPaysField.setItems(allCountries);
//        dialogPaysField.setPlaceholder("Choisir un pays...");
//
//
//        formLayout.add(dialogNomField, dialogDateDebutField, dialogDateFinField, dialogPaysField);
//
//        dialogBinder = new Binder<>(JourFerie.class);
//        dialogBinder.forField(dialogNomField)
//                .asRequired("Le nom ne peut pas être vide")
//                .bind(JourFerie::getNom, JourFerie::setNom);
//
//        dialogBinder.forField(dialogDateDebutField)
//                .asRequired("La date de début est obligatoire")
//                .bind(JourFerie::getDateDebut, JourFerie::setDateDebut);
//
//        dialogBinder.forField(dialogDateFinField)
//                .asRequired("La date de fin est obligatoire")
//                .withValidator(dateFinValue -> dateFinValue == null || dialogDateDebutField.getValue() == null ||
//                                !dateFinValue.isBefore(dialogDateDebutField.getValue()),
//                        "La date de fin doit être postérieure à la date de début")
//                .bind(JourFerie::getDateFin, JourFerie::setDateFin);
//
//        dialogBinder.forField(dialogPaysField)
//                .asRequired("Le pays ne peut pas être vide")
//                .bind(JourFerie::getPays, JourFerie::setPays);
//
//
//
//        Button saveButton = new Button("Enregistrer", e -> {
//            try {
//                JourFerie newJourFerie = new JourFerie();
//                dialogBinder.writeBean(newJourFerie);
//                JourFerie savedJourFerie = holidaySyncService.saveJourFerie(newJourFerie);
//                refreshGrid();
//                dialog.close();
//                showSuccessNotification("Jour férié créé avec succès !");
//
//            } catch (ValidationException ex) {
//                showErrorNotification("Veuillez corriger les erreurs dans le formulaire");
//            } catch (Exception ex) {
//                showErrorNotification("Erreur lors de la création : " + ex.getMessage());
//            }
//        });
//        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//
//        Button cancelButton = new Button("Annuler", e -> dialog.close());
//        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
//
//        dialog.add(formLayout);
//        dialog.getFooter().add(cancelButton, saveButton);
//
//        return dialog;
//    }
//    private void clearDialogForm() {
//        if (dialogNomField != null) dialogNomField.clear();
//        if (dialogDateDebutField != null) dialogDateDebutField.clear();
//        if (dialogDateFinField != null) dialogDateFinField.clear();
//        if (dialogPaysField != null) dialogPaysField.clear();
//        if (dialogBinder != null) dialogBinder.readBean(new JourFerie());
//    }
//
//    private void setupGridColumns(Binder<JourFerie> binder) {
//        Grid.Column<JourFerie> nom = grid.addColumn(JourFerie::getNom).setHeader("Nom").setSortable(true);
//        TextField nomField = new TextField();
//        nomField.setWidthFull();
//        binder.forField(nomField)
//                .asRequired("Le nom ne peut pas être vide")
//                .bind(JourFerie::getNom, JourFerie::setNom);
//        nom.setEditorComponent(nomField);
//
//        Grid.Column<JourFerie> dateDebut = grid.addColumn(JourFerie::getDateDebut).setHeader("Date de Début").setSortable(true);
//        DatePicker dateDebutField = new DatePicker();
//        dateDebutField.setWidthFull();
//        binder.forField(dateDebutField)
//                .asRequired("La date de début est obligatoire")
//                .bind(JourFerie::getDateDebut, JourFerie::setDateDebut);
//        dateDebut.setEditorComponent(dateDebutField);
//
//        Grid.Column<JourFerie> dateFin = grid.addColumn(JourFerie::getDateFin).setHeader("Date de Fin").setSortable(true);
//        DatePicker dateFinField = new DatePicker();
//        dateFinField.setWidthFull();
//        binder.forField(dateFinField)
//                .asRequired("La date de fin est obligatoire")
//                .withValidator(dateFinValue -> dateFinValue == null || dateDebutField.getValue() == null ||
//                                !dateFinValue.isBefore(dateDebutField.getValue()),
//                        "La date de fin doit être postérieure à la date de début")
//                .bind(JourFerie::getDateFin, JourFerie::setDateFin);
//        dateFin.setEditorComponent(dateFinField);
//
//        Grid.Column<JourFerie> pays = grid.addColumn(JourFerie::getPays).setHeader("Pays").setSortable(true);
//        TextField paysField = new TextField();
//        paysField.setWidthFull();
//        binder.forField(paysField)
//                .asRequired("Le pays ne peut pas être vide")
//                .bind(JourFerie::getPays, JourFerie::setPays);
//        pays.setEditorComponent(paysField);
//
//        Grid.Column<JourFerie> paysCode = grid.addColumn(JourFerie::getPaysCode).setSortable(true)
//                .setHeader("Code ISO")
//                .setWidth("120px")
//                .setFlexGrow(0);
//        TextField paysCodeField = new TextField();
//        paysCodeField.setWidthFull();
//        binder.forField(paysCodeField)
//                .asRequired("Le code ISO ne peut pas être vide")
//                .withValidator(code -> code.length() == 2 || code.length() == 3,
//                        "Le code ISO doit contenir 2 ou 3 caractères")
//                .bind(JourFerie::getPaysCode, JourFerie::setPaysCode);
//        paysCode.setEditorComponent(paysCodeField);
//
//        Grid.Column<JourFerie> editColumn = grid.addComponentColumn(jourferie -> {
//            Button editButton = new Button("Éditer");
//            editButton.addClickListener(e -> {
//                if (editor.isOpen())
//                    editor.cancel();
//                grid.getEditor().editItem(jourferie);
//            });
//            return editButton;
//        });
//
//        Button saveButton = new Button("Sauvegarder", e -> {
//            if (editor.save()) {
//                showSuccessNotification("Jour férié sauvegardé avec succès !");
//            }
//        });
//        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//
//        Button cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
//        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
//
//        HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
//        actions.setPadding(false);
//        editColumn.setEditorComponent(actions);
//    }
//
//    private void setupEditorEvents() {
//        editor.addSaveListener(event -> {
//            try {
//                JourFerie jourFerie = event.getItem();
//                JourFerie savedJourFerie = holidaySyncService.saveJourFerie(jourFerie);
//                refreshGrid();
//                showSuccessNotification("Jour férié mis à jour avec succès !");
//            } catch (Exception e) {
//                showErrorNotification("Erreur lors de la sauvegarde : " + e.getMessage());
//                editor.cancel();
//            }
//        });
//    }
//
//
//    private void setupSearchFilter() {
//        searchField.addValueChangeListener(e -> {
//            if (dataView != null) {
//                dataView.refreshAll();
//            }
//        });
//
//        dataView.addFilter(jourFerie -> {
//            String searchTerm = searchField.getValue();
//            if (searchTerm == null || searchTerm.trim().isEmpty()) {
//                return true;
//            }
//
//            searchTerm = searchTerm.trim().toLowerCase();
//            boolean matchesName = jourFerie.getNom() != null &&
//                    jourFerie.getNom().toLowerCase().contains(searchTerm);
//            boolean matchesPays = jourFerie.getPays() != null &&
//                    jourFerie.getPays().toLowerCase().contains(searchTerm);
//
//            return matchesName || matchesPays;
//        });
//    }
//
//    private void loadAllHolidays() {
//        try {
//            List<JourFerie> allHolidays = holidaySyncService.getAllJourFerie();
//            if (grid != null) {
//                dataView = grid.setItems(allHolidays);
//                setupSearchFilter();
//            }
//        } catch (Exception e) {
//            showErrorNotification("Erreur lors du chargement de tous les jours fériés : " + e.getMessage());
//        }
//    }
//
//    private void loadHolidaysForCountry(String countryName) {
//        try {
//            List<JourFerie> holidays = holidaySyncService.getHolidaysByCountry(countryName);
//            if (grid != null) {
//                dataView = grid.setItems(holidays);
//                setupSearchFilter();
//            }
//        } catch (Exception e) {
//            showErrorNotification("Erreur lors du chargement des jours fériés : " + e.getMessage());
//        }
//    }
//
//    private void refreshGrid() {
//        if (currentSelectedCountry != null) {
//            loadHolidaysForCountry(currentSelectedCountry);
//        } else {
//            loadAllHolidays();
//        }
//    }
//
//    private boolean matchesTerm(String value, String searchTerm) {
//        if (value == null || searchTerm == null) {
//            return false;
//        }
//        return value.toLowerCase().contains(searchTerm.toLowerCase());
//    }
//
//    private void showSuccessNotification(String message) {
//        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
//        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//    }
//
//    private void showErrorNotification(String message) {
//        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
//        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//    }
//}