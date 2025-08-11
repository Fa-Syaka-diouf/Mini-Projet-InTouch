package com.elfstack.toys.security.dev;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import jakarta.servlet.http.HttpServletResponse;

@Route("error")
public class CustomErrorView extends VerticalLayout implements HasErrorParameter<Exception> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<Exception> parameter) {
        removeAll();
        add(new H1("Une erreur s'est produite"));
        add("Désolé, mais la page demandée n'est pas disponible.");
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
}
