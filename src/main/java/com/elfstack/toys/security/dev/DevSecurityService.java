package com.elfstack.toys.security.dev;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.VaadinSession;

import java.util.Objects;

@Service
public class DevSecurityService {
    private final OAuth2AuthorizedClientService clientService;

    @Autowired
    public DevSecurityService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }
    public OAuth2AuthenticationToken getAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
            return (OAuth2AuthenticationToken) authentication;
    }

    public String getCurrentUsername() {
        var user = getAuthenticatedUser();
        return Objects.requireNonNull(user.getPrincipal().getAttribute("preferred_username"));
    }
    public String getIdTokenValue() {
        var auth = getAuthenticatedUser();
        var principal = auth.getPrincipal();

        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getIdToken().getTokenValue();
        }
        return null;
    }
    public void logout() {
        SecurityContextHolder.clearContext();
        VaadinSession.getCurrent().getSession().invalidate();
    }
}