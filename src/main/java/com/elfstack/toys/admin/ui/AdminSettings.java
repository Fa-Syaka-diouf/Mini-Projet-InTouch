package com.elfstack.toys.admin.ui;


import com.elfstack.toys.admin.service.HolidaySyncService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Route(value = "admin/settings", layout = AdminLayout.class)
@Menu(order = 2, title = "Parametre")
@PageTitle("Admin Page")
@RolesAllowed("ADMIN")
public class AdminSettings extends Main {

    HolidaySyncService holidaySyncService;

    public AdminSettings(HolidaySyncService holidaySyncService) {
        this.holidaySyncService = holidaySyncService;

        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.setSizeFull();
        centerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        centerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        ConfirmDialog dialog = new ConfirmDialog();
        int currentYear = LocalDate.now().getYear();
        dialog.setHeader("Renouvellement Calendrier " + currentYear);

        dialog.setText(
                "Êtes-vous sûr de vouloir renouveler la base de données des jours fériés ?\n" +
                        "Cette action remplacera les données actuelles par les informations les plus récentes.");

        dialog.setCancelable(true);

        dialog.setConfirmText("Renouveller");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> startSynchronization());

        Button button = new Button("Renouveller la table des jours feries");
        button.addClickListener(event -> {
            dialog.open();
        });

        centerLayout.add(button);

        add(centerLayout);
    }

    private void startSynchronization() {
        int currentYear = LocalDate.now().getYear();

        Notification.show("Renouvellement commence...", 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        long start = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> {

            try {
                holidaySyncService.syncAllCountries(currentYear);
            } catch (Exception e) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("Erreur lors du chargement de la base: " + e.getMessage(),
                                    3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }));
            }
        });
        long end = System.currentTimeMillis();
        long duration = end - start;
        getUI().ifPresent(ui -> ui.access(() -> {
            Notification.show("Renouvellement terminé en " + duration + " ms",
                            (int) duration, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }));
    }
}
