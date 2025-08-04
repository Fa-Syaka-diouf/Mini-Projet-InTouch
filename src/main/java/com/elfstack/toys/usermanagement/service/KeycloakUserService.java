//package com.elfstack.toys.usermanagement.service;
//
//import com.elfstack.toys.usermanagement.domain.KeycloakUserDto;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class KeycloakUserService {
//
//    private final RestTemplate restTemplate;
//    private final String baseUrl;
//
//    public KeycloakUserService(RestTemplate restTemplate,
//                               @Value("${keycloak.api.url}") String baseUrl) {
//        this.restTemplate = restTemplate;
//        this.baseUrl = baseUrl;
//    }
//
//    public List<KeycloakUserDto> getAllUsers() {
//        String url = "http://localhost:8080/auth/admin/realms/task-management/users";
//        ResponseEntity<KeycloakUserDto[]> response = restTemplate.getForEntity(baseUrl + "/users", KeycloakUserDto[].class);
//        KeycloakUserDto[] users = response.getBody();
//        return users != null ? Arrays.asList(users) : List.of();
//    }
//
//
//    public List<String> getAllUsernames() {
//        return getAllUsers().stream()
//                .map(KeycloakUserDto::getUsername)
//                .collect(Collectors.toList());
//    }
//
//}

package com.elfstack.toys.usermanagement.service;

import com.elfstack.toys.usermanagement.domain.KeycloakUserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeycloakUserService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public KeycloakUserService(RestTemplate restTemplate,
                               @Value("${keycloak.api.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<KeycloakUserDto> getAllUsers() {
        try {
            // Option 1: Utiliser l'URL complète (recommandé pour le debug)
            String fullUrl = "http://localhost:8080/auth/admin/realms/task-management/users";

            // Debug: afficher l'URL utilisée
            System.out.println("URL Keycloak utilisée: " + fullUrl);
            System.out.println("BaseUrl configurée: " + baseUrl);

            // Créer les headers appropriés
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.set("Content-Type", "application/json");

            // TODO: Ajouter l'authentification si nécessaire
            // headers.setBearerAuth(getAccessToken());

            HttpEntity<?> entity = new HttpEntity<>(headers);

            // Première tentative avec l'URL complète
            ResponseEntity<KeycloakUserDto[]> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    KeycloakUserDto[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                KeycloakUserDto[] users = response.getBody();
                System.out.println("Utilisateurs récupérés: " + users.length);
                return Arrays.asList(users);
            }

        } catch (RestClientException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs Keycloak: " + e.getMessage());

            // Debug: essayer de récupérer la réponse brute
            try {
                String rawResponse = restTemplate.getForObject(
                        "http://localhost:8080/auth/admin/realms/task-management/users",
                        String.class
                );
                System.out.println("Réponse brute Keycloak: " + rawResponse);
            } catch (Exception debugEx) {
                System.err.println("Impossible de récupérer la réponse brute: " + debugEx.getMessage());
            }
        }

        return List.of(); // Retourner une liste vide en cas d'erreur
    }

    // Version alternative utilisant baseUrl
    public List<KeycloakUserDto> getAllUsersWithBaseUrl() {
        try {
            String url = baseUrl + "/users";
            System.out.println("URL construite: " + url);

            ResponseEntity<KeycloakUserDto[]> response = restTemplate.getForEntity(url, KeycloakUserDto[].class);
            KeycloakUserDto[] users = response.getBody();
            return users != null ? Arrays.asList(users) : List.of();

        } catch (Exception e) {
            System.err.println("Erreur avec baseUrl: " + e.getMessage());
            return List.of();
        }
    }

    public List<String> getAllUsernames() {
        return getAllUsers().stream()
                .map(KeycloakUserDto::getUsername)
                .filter(username -> username != null && !username.trim().isEmpty())
                .collect(Collectors.toList());
    }

    // Méthode utilitaire pour tester la connexion Keycloak
    public boolean testKeycloakConnection() {
        try {
            String testUrl = "http://localhost:8080/auth/admin/realms/task-management";
            ResponseEntity<String> response = restTemplate.getForEntity(testUrl, String.class);
            System.out.println("Test connexion Keycloak - Status: " + response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Test connexion Keycloak échoué: " + e.getMessage());
            return false;
        }
    }
}