package com.elfstack.toys.admin.ui;


import com.elfstack.toys.base.ui.component.ViewToolbar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
@PageTitle("Admin Page")
@Menu(order = 0, title = "Dashboard")
public final class AdminPage extends Main {

        AdminPage() {
            addClassName(LumoUtility.Padding.MEDIUM);
            add(new Div("Please select a view from the menu on the left."));
        }
}
