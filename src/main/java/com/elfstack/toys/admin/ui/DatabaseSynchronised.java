package com.elfstack.toys.admin.ui;


import com.elfstack.toys.admin.service.HolidaySyncService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Route(value = "admin/Renouvellement", layout = AdminLayout.class)
@Menu(order = 2, title = "Renouvellement de Base")
@PageTitle("Admin Page")
@PermitAll
public class DatabaseSynchronised extends Main {

    HolidaySyncService holidaySyncService;

    public DatabaseSynchronised(HolidaySyncService holidaySyncService) {
        this.holidaySyncService = holidaySyncService;
        setupUI();
    }

    @PostConstruct
    public void afterViewInit() {
        startSynchronization();
    }

    private void setupUI() {
        add(new H1("Renouvellement de la base de données"));
        add(new Div("Synchronisation en cours..."));
    }

    private void startSynchronization() {
        int currentYear = LocalDate.now().getYear();

        Notification.show("Renouvellement commence...", 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_CONTRAST);

        // Exécuter la sync de manière asynchrone
        CompletableFuture.runAsync(() -> {
            long start = System.currentTimeMillis();
            try {
                holidaySyncService.syncAllCountries(currentYear);

                long end = System.currentTimeMillis();
                long duration = end - start;

                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("Renouvellement terminé en " + duration + " ms",
                                    (int) duration, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }));

            } catch (Exception e) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("Erreur lors du chargement de la base: " + e.getMessage(),
                                    3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }));
            }
        });
    }
}

