package com.elfstack.toys.admin.ui;

import com.elfstack.toys.admin.domain.JourFerie;
import com.elfstack.toys.admin.service.HolidaySyncService;
import com.elfstack.toys.base.ui.component.ViewToolbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "admin/JourFerie", layout = AdminLayout.class)
@PageTitle("Admin Page")
@PermitAll
@Menu(order = 1, title = "Jours Fériés")
public class AdminJourFerie extends Main {

    private final HolidaySyncService holidaySyncService;
    private Grid<JourFerie> grid;
    private Editor<JourFerie> editor;
    private final List<String> allCountries;
    private String currentSelectedCountry;

    public AdminJourFerie(HolidaySyncService holidaySyncService) {
        this.holidaySyncService = holidaySyncService;
        allCountries = holidaySyncService.getAllCountries();
        setupUI();
        // Charger tous les jours fériés par défaut au démarrage
        loadAllHolidays();
    }

    private void setupUI() {
        setSizeFull();
        // Titre avec style amélioré
        H1 title = new H1("Zone Administration - Jours Fériés");
        title.getStyle()
                .set("color", "#2c3e50")
                .set("text-align", "center")
                .set("margin-bottom", "2rem")
                .set("font-weight", "600")
                .set("text-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        add(new H1("Zone Admin"));

        ComboBox<String> countryComboBox = new ComboBox<>("Sélectionner un pays");
        countryComboBox.setItems(allCountries);
        countryComboBox.setPlaceholder("Choisir un pays...");
        countryComboBox.addValueChangeListener(event -> {
            String selectedCountry = event.getValue();
            if (selectedCountry != null) {
                currentSelectedCountry = selectedCountry;
                loadHolidaysForCountry(selectedCountry);
            } else {
                // Si aucun pays n'est sélectionné, afficher tous les jours fériés
                currentSelectedCountry = null;
                loadAllHolidays();
            }
        });

        // Ajouter un bouton pour afficher tous les jours fériés
        Button showAllButton = new Button("Afficher tous les jours fériés");
        showAllButton.addClickListener(e -> {
            countryComboBox.clear();
            currentSelectedCountry = null;
            loadAllHolidays();
        });
        showAllButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        grid = new Grid<>(JourFerie.class, false);
        editor = grid.getEditor();

        Binder<JourFerie> binder = new Binder<>(JourFerie.class);
        editor.setBinder(binder);
        editor.setBuffered(true);
        setupGridColumns(binder);
        setupEditorEvents();

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        // Créer un layout horizontal pour la toolbar avec le bouton
        HorizontalLayout toolbarLayout = new HorizontalLayout(countryComboBox, showAllButton);
        toolbarLayout.setAlignItems(HorizontalLayout.Alignment.END);
        toolbarLayout.setWidthFull();

        add(new ViewToolbar("Liste des Jours Fériés", toolbarLayout));
        add(grid);
    }

    private void setupGridColumns(Binder<JourFerie> binder) {
        Grid.Column<JourFerie> nom = grid.addColumn(JourFerie::getNom).setHeader("Nom").setSortable(true);
        TextField nomField = new TextField();
        nomField.setWidthFull();
        binder.forField(nomField)
                .asRequired("Le nom ne peut pas être vide")
                .bind(JourFerie::getNom, JourFerie::setNom);
        nom.setEditorComponent(nomField);

        Grid.Column<JourFerie> dateDebut = grid.addColumn(JourFerie::getDateDebut).setHeader("Date de Début").setSortable(true);
        DatePicker dateDebutField = new DatePicker();
        dateDebutField.setWidthFull();
        binder.forField(dateDebutField)
                .asRequired("La date de début est obligatoire")
                .bind(JourFerie::getDateDebut, JourFerie::setDateDebut);
        dateDebut.setEditorComponent(dateDebutField);

        Grid.Column<JourFerie> dateFin = grid.addColumn(JourFerie::getDateFin).setHeader("Date de Fin").setSortable(true);
        DatePicker dateFinField = new DatePicker();
        dateFinField.setWidthFull();
        binder.forField(dateFinField)
                .asRequired("La date de fin est obligatoire")
                .withValidator(dateFinValue -> dateFinValue == null || dateDebutField.getValue() == null ||
                                !dateFinValue.isBefore(dateDebutField.getValue()),
                        "La date de fin doit être postérieure à la date de début")
                .bind(JourFerie::getDateFin, JourFerie::setDateFin);
        dateFin.setEditorComponent(dateFinField);

        Grid.Column<JourFerie> pays = grid.addColumn(JourFerie::getPays).setHeader("Pays").setSortable(true);
        TextField paysField = new TextField();
        paysField.setWidthFull();
        binder.forField(paysField)
                .asRequired("Le pays ne peut pas être vide")
                .bind(JourFerie::getPays, JourFerie::setPays);
        pays.setEditorComponent(paysField);

        Grid.Column<JourFerie> paysCode = grid.addColumn(JourFerie::getPaysCode).setSortable(true)
                .setHeader("Code ISO")
                .setWidth("120px")
                .setFlexGrow(0);
        TextField paysCodeField = new TextField();
        paysCodeField.setWidthFull();
        binder.forField(paysCodeField)
                .asRequired("Le code ISO ne peut pas être vide")
                .withValidator(code -> code.length() == 2 || code.length() == 3,
                        "Le code ISO doit contenir 2 ou 3 caractères")
                .bind(JourFerie::getPaysCode, JourFerie::setPaysCode);
        paysCode.setEditorComponent(paysCodeField);

        Grid.Column<JourFerie> editColumn = grid.addComponentColumn(jourferie -> {
            Button editButton = new Button("Éditer");
            editButton.addClickListener(e -> {
                if (editor.isOpen())
                    editor.cancel();
                grid.getEditor().editItem(jourferie);
            });
            return editButton;
        });

        Button saveButton = new Button("Sauvegarder", e -> {
            if (editor.save()) {
                showSuccessNotification("Jour férié sauvegardé avec succès !");
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);

        HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
        actions.setPadding(false);
        editColumn.setEditorComponent(actions);
    }

    private void setupEditorEvents() {
        editor.addSaveListener(event -> {
            try {
                JourFerie jourFerie = event.getItem();
                JourFerie savedJourFerie = holidaySyncService.saveJourFerie(jourFerie);
                refreshGrid();
                showSuccessNotification("Jour férié mis à jour avec succès !");
            } catch (Exception e) {
                showErrorNotification("Erreur lors de la sauvegarde : " + e.getMessage());
                editor.cancel();
            }
        });
    }

    /**
     * Charge tous les jours fériés du monde
     */
    private void loadAllHolidays() {
        try {
            List<JourFerie> allHolidays = holidaySyncService.getAllJourFerie();
            if (grid != null) {
                grid.setItems(allHolidays);
            }
        } catch (Exception e) {
            showErrorNotification("Erreur lors du chargement de tous les jours fériés : " + e.getMessage());
        }
    }

    private void loadHolidaysForCountry(String countryName) {
        try {
            List<JourFerie> holidays = holidaySyncService.getHolidaysByCountry(countryName);
            if (grid != null) {
                grid.setItems(holidays);
            }
        } catch (Exception e) {
            showErrorNotification("Erreur lors du chargement des jours fériés : " + e.getMessage());
        }
    }

    private void refreshGrid() {
        if (currentSelectedCountry != null) {
            loadHolidaysForCountry(currentSelectedCountry);
        } else {
            // Si aucun pays n'est sélectionné, recharger tous les jours fériés
            loadAllHolidays();
        }
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