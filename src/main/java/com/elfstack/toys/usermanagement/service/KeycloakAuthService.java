package com.elfstack.toys.usermanagement.service;

import com.elfstack.toys.usermanagement.domain.KeycloakProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class KeycloakAuthService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAuthService.class);

    private final KeycloakProperties keycloakProperties;
    private final RestTemplate restTemplate;

    // Cache du token
    private String cachedToken;
    private LocalDateTime tokenExpirationTime;

    public KeycloakAuthService(KeycloakProperties keycloakProperties, RestTemplate restTemplate) {
        this.keycloakProperties = keycloakProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * Récupère un token d'accès admin pour Keycloak avec cache
     */
    public String getAdminToken() {
        // Vérifier si le token est encore valide (avec marge de 30 secondes)
        if (cachedToken != null && tokenExpirationTime != null &&
                LocalDateTime.now().isBefore(tokenExpirationTime.minusSeconds(30))) {
            log.debug("Utilisation du token en cache");
            return cachedToken;
        }

        log.info("Récupération d'un nouveau token admin Keycloak");

        try {
            String tokenUrl = buildTokenUrl();
            HttpEntity<MultiValueMap<String, String>> request = buildTokenRequest();

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String accessToken = (String) responseBody.get("access_token");
                Integer expiresIn = (Integer) responseBody.get("expires_in");

                if (accessToken != null) {
                    // Mettre en cache le token
                    cachedToken = accessToken;
                    if (expiresIn != null) {
                        tokenExpirationTime = LocalDateTime.now().plusSeconds(expiresIn);
                    } else {
                        // Par défaut, considérer que le token expire dans 5 minutes
                        tokenExpirationTime = LocalDateTime.now().plusMinutes(5);
                    }

                    log.info("Token admin récupéré avec succès, expire à : {}", tokenExpirationTime);
                    return accessToken;
                }
            }

            throw new RuntimeException("Réponse invalide de Keycloak lors de la récupération du token");

        } catch (RestClientException e) {
            log.error("Erreur lors de la récupération du token Keycloak", e);
            throw new RuntimeException("Impossible d'obtenir le token d'accès de Keycloak: " + e.getMessage(), e);
        }
    }

    public void invalidateToken() {
        log.info("Invalidation du token en cache");
        cachedToken = null;
        tokenExpirationTime = null;
    }

    private String buildTokenUrl() {
        String baseUrl = keycloakProperties.getAuthServerUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8081";
        }
        return baseUrl + "/realms/master/protocol/openid-connect/token";
    }

    private HttpEntity<MultiValueMap<String, String>> buildTokenRequest() {
        // Validation des propriétés
        if (keycloakProperties.getAdminClient() == null ||
                keycloakProperties.getAdminClient().getClientId() == null ||
                keycloakProperties.getAdminClient().getClientSecret() == null) {
            throw new IllegalStateException("Configuration Keycloak incomplète");
        }

        String clientId = keycloakProperties.getAdminClient().getClientId();
        String clientSecret = keycloakProperties.getAdminClient().getClientSecret();

        // Corps de la requête
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "TaskManagement-App/1.0");

        return new HttpEntity<>(body, headers);
    }
}
