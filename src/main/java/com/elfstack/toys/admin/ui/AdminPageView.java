package com.elfstack.toys.admin.ui;

import com.elfstack.toys.admin.domain.JourFerie;
import com.elfstack.toys.admin.service.HolidaySyncService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "admin", layout = AdminLayout.class)
@PageTitle("Admin Page")
@PermitAll
public class AdminPageView extends Main {

    private final HolidaySyncService holidaySyncService;
    private Grid<JourFerie> grid;
    private Editor<JourFerie> editor;
    private final List<String> allCountries;


    public AdminPageView(HolidaySyncService holidaySyncService) {
        this.holidaySyncService = holidaySyncService;
        allCountries = holidaySyncService.getAllCountries();
        setupUI();

    }

    private void setupUI() {
        setSizeFull();
        add(new H1("Zone Admin"));
        ComboBox<String> countryComboBox = new ComboBox<>("Sélectionner un pays");
        countryComboBox.setItems(allCountries);
        countryComboBox.setPlaceholder("Choisir un pays...");
        countryComboBox.addValueChangeListener(event -> {
            String selectedCountry = event.getValue();
            if (selectedCountry != null) {
                loadHolidaysForCountry(selectedCountry);
            }
        });
        add(countryComboBox);

        grid = new Grid<>(JourFerie.class, false);
        editor = grid.getEditor();
        grid.addColumn(JourFerie::getNom).setHeader("Nom");
        grid.addColumn(JourFerie::getDateDebut).setHeader("Date de Début");
        grid.addColumn(JourFerie::getDateFin).setHeader("Date de Fin");
        grid.addColumn(JourFerie::getPaysCode).setHeader("Pays");
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setEmptyStateText("Aucun Jour trouve pour ce pays.");
        grid.setSizeFull();
        add(grid);
    }

    private void loadHolidaysForCountry(String countryName) {
        List<JourFerie> holidays = holidaySyncService.getHolidaysByCountry(countryName);
        if (grid != null) {
            grid.setItems(holidays);
        }
    }
}