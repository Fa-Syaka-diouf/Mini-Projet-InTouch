package com.elfstack.toys.base.ui.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "task-list" , layout = MainLayout.class)
@RolesAllowed({"ADMIN", "USER"})
public class MainTask extends Main {

    MainTask() {
        addClassName(LumoUtility.Padding.MEDIUM);
        add(new Div("Liste des taches page"));
    }
}
