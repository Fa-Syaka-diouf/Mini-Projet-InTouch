package com.elfstack.toys.security.dev;

import com.elfstack.toys.base.ui.view.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import jakarta.servlet.http.HttpServletResponse;


@Route(value = "error/405",layout = MainLayout.class)
public class MethodNotAllowedErrorView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        removeAll();
        add(new H1("Erreur 405 - Méthode non autorisée"));
        add("La méthode HTTP que vous avez utilisée n'est pas supportée !");
        return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
}
