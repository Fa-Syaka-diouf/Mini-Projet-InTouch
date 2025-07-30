package com.elfstack.toys.base.ui.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "notifications" , layout = MainLayout.class)
@RolesAllowed({"ADMIN", "USER"})
public class MainNotification extends Main {

    MainNotification() {
        addClassName(LumoUtility.Padding.MEDIUM);
        add(new Div("Notifications page"));
    }
}
