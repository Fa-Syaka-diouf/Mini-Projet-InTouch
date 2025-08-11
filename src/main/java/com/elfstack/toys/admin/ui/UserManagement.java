package com.elfstack.toys.admin.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/user-management", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class UserManagement extends Main {
    UserManagement(){
        String keycloakUrl = "http://localhost:8081/admin/master/console/#/task-management/users";
        UI.getCurrent().getPage().setLocation(keycloakUrl);
    }
}
