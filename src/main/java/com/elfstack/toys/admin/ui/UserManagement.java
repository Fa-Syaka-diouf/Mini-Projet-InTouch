package com.elfstack.toys.admin.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/user-management", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class UserManagement extends Main {
    UserManagement(){
        addClassName(LumoUtility.Padding.MEDIUM);
        add(new Div("User management"));
    }
}
