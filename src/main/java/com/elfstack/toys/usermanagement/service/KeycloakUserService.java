package com.elfstack.toys.usermanagement.service;

import com.elfstack.toys.usermanagement.domain.KeycloakUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KeycloakUserService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserService.class);

    private final RestTemplate restTemplate;
    private final KeycloakAuthService authService;
    private final String baseUrl;
    private final String realm;

    public KeycloakUserService(RestTemplate restTemplate,
                               KeycloakAuthService authService,
                               @Value("${keycloak.server-url:http://localhost:8081}") String baseUrl,
                               @Value("${keycloak.realm:task-management}") String realm) {
        this.restTemplate = restTemplate;
        this.authService = authService;
        this.baseUrl = baseUrl;
        this.realm = realm;
    }

    /**
     * Récupère tous les utilisateurs du realm Keycloak
     */
    public List<KeycloakUserDto> getAllUsers() {
        return getAllUsers(0, 100); // Par défaut, récupérer les 100 premiers
    }

    /**
     * Récupère les utilisateurs avec pagination
     */
    public List<KeycloakUserDto> getAllUsers(int first, int max) {
        log.info("Récupération des utilisateurs Keycloak (first: {}, max: {})", first, max);

        try {
            String url = buildUsersUrl(first, max);
            HttpHeaders headers = createAuthenticatedHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<KeycloakUserDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<KeycloakUserDto>>() {}
            );

            List<KeycloakUserDto> users = response.getBody();
            if (users != null) {
                log.info("Récupéré {} utilisateurs avec succès", users.size());
                return users;
            } else {
                log.warn("Réponse vide de Keycloak");
                return Collections.emptyList();
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Token expiré, tentative de renouvellement");
            authService.invalidateToken();

            // Retry une seule fois avec un nouveau token
            try {
                String url = buildUsersUrl(first, max);
                HttpHeaders headers = createAuthenticatedHeaders();
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<List<KeycloakUserDto>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<KeycloakUserDto>>() {}
                );

                List<KeycloakUserDto> users = response.getBody();
                log.info("Récupéré {} utilisateurs après renouvellement du token",
                        users != null ? users.size() : 0);
                return users != null ? users : Collections.emptyList();

            } catch (Exception retryException) {
                log.error("Échec de la récupération des utilisateurs même après renouvellement du token", retryException);
                throw new RuntimeException("Impossible de récupérer les utilisateurs après renouvellement du token", retryException);
            }

        } catch (RestClientException e) {
            log.error("Erreur lors de la récupération des utilisateurs Keycloak", e);
            throw new RuntimeException("Erreur lors de la communication avec Keycloak: " + e.getMessage(), e);
        }
    }

    public List<String> getAllUsernames() {
        try {
            List<KeycloakUserDto> users = getAllUsers();
            List<String> username = users.stream()
                    .filter(user -> user.getUsername() != null && !user.getUsername().trim().isEmpty())
                    .map(KeycloakUserDto::getUsername)
                    .sorted()
                    .collect(Collectors.toList());

            log.info("Récupéré {} noms d'utilisateur", username.size());
            return username;

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des noms d'utilisateur", e);
            return Collections.emptyList();
        }
    }
    /**
     * Récupère l'ID d'un utilisateur à partir de son fullName
     */
    public String getUserIdByUserName(String username) {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Le fullName fourni est vide ou null");
            return null;
        }

        try {
            List<KeycloakUserDto> users = getAllUsers();
            Optional<KeycloakUserDto> user = users.stream()
                    .filter(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(username.trim()))
                    .findFirst();

            if (user.isPresent()) {
                log.info("Utilisateur trouvé : {} avec ID {}", username, user.get().getId());
                return user.get().getId();
            } else {
                log.warn("Aucun utilisateur trouvé avec le fullName : {}", username);
                return null;
            }

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'ID de l'utilisateur avec fullName: {}", username, e);
            return null;
        }
    }
    public String getFullNameByUserName(String username) {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Le fullName fourni est vide ou null");
            return null;
        }

        try {
            List<KeycloakUserDto> users = getAllUsers();
            Optional<KeycloakUserDto> user = users.stream()
                    .filter(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(username.trim()))
                    .findFirst();

            if (user.isPresent()) {
                log.info("Utilisateur trouvé : {} avec nom complet {}", username, user.get().getFullName());
                return user.get().getFullName();
            } else {
                log.warn("Aucun utilisateur trouvé avec le fullName : {}", username);
                return null;
            }

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'ID de l'utilisateur avec fullName: {}", username, e);
            return null;
        }
    }


    public List<KeycloakUserDto> getActiveUsers() {
        try {
            return getAllUsers().stream()
                    .filter(user -> user.isEnabled())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs actifs", e);
            return Collections.emptyList();
        }
    }

    public KeycloakUserDto getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        try {
            String url = buildUsersUrl(0, 1) + "&username=" + username.trim();
            HttpHeaders headers = createAuthenticatedHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<KeycloakUserDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<KeycloakUserDto>>() {}
            );

            List<KeycloakUserDto> users = response.getBody();
            if (users != null && !users.isEmpty()) {
                return users.get(0);
            }

            return null;

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur: {}", username, e);
            return null;
        }
    }

    private String buildUsersUrl(int first, int max) {
        return String.format("%s/admin/realms/%s/users?first=%d&max=%d",
                baseUrl, realm, first, max);
    }

    private HttpHeaders createAuthenticatedHeaders() {
        String token = authService.getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "TaskManagement-App/1.0");
        return headers;
    }

    /**
     * Test de connectivité avec Keycloak
     */
    public boolean testConnection() {
        try {
            getAllUsers(0, 1);
            return true;
        } catch (Exception e) {
            log.error("Test de connexion Keycloak échoué", e);
            return false;
        }
    }
}
