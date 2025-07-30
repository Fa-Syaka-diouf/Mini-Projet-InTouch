package com.elfstack.toys.security.dev;

import com.vaadin.flow.component.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.VaadinSession;

import java.util.Map;
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
    public boolean hasRole(String role) {
        var auth = getAuthenticatedUser();

        return auth.getAuthorities().stream()
                .anyMatch(authority -> {
                    String authRole = authority.getAuthority();
                    return authRole.equalsIgnoreCase("ROLE_" + role) ||
                            authRole.equalsIgnoreCase("ROLE_" + role.toUpperCase()) ||
                            authRole.equalsIgnoreCase("ROLE_" + role.toLowerCase());
                });
    }
    public boolean isAdmin() {
        return hasRole("admin");
    }

    public boolean isUser() {
        return hasRole("user");
    }

    public Map<String, Object> getAllAttributes() {
        return getAuthenticatedUser().getPrincipal().getAttributes();
    }

    public void logout() {
        String idToken = getIdTokenValue();
        String logoutUrl = "http://localhost:8081/realms/task-management/protocol/openid-connect/logout" + "?id_token_hint=" + idToken
                + "&post_logout_redirect_uri=http://localhost:8080/";
        UI.getCurrent().getPage().setLocation(logoutUrl);
        SecurityContextHolder.clearContext();
        VaadinSession.getCurrent().getSession().invalidate();
    }
    public void debugTokenClaims() {
        var auth = getAuthenticatedUser();
        var principal = auth.getPrincipal();

        if (principal instanceof OidcUser oidcUser) {
            System.out.println("=== DEBUG TOKEN CLAIMS ===");
            Map<String, Object> claims = oidcUser.getClaims();
            claims.forEach((key, value) -> {
                System.out.println(key + ": " + value);
            });
            System.out.println("=========================");
        }
    }
}