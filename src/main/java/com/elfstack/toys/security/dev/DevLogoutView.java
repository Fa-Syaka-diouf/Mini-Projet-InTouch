package com.elfstack.toys.security.dev;

import com.elfstack.toys.security.dev.DevSecurityService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;

@Route("/logout")
@Menu(order = 1, title = "logout")
@PageTitle("Déconnexion")
@PermitAll
public class DevLogoutView extends Main {

    private final DevSecurityService devSecurityService;

    public DevLogoutView(DevSecurityService devSecurityService) {
        this.devSecurityService = devSecurityService;
    }

    @PostConstruct
    private void init() {
        String idToken = devSecurityService.getIdTokenValue();
        String logoutUrl = "http://localhost:8081/realms/task-management/protocol/openid-connect/logout" + "?id_token_hint=" + idToken
                + "&post_logout_redirect_uri=http://localhost:8080/";
        UI.getCurrent().getPage().setLocation(logoutUrl);

        devSecurityService.logout();


    }
}
