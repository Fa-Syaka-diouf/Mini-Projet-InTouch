package com.elfstack.toys.usermanagement.service;

import com.elfstack.toys.usermanagement.domain.KeycloakProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KeycloakAuthService {
    private final KeycloakProperties keycloakProperties;

    public KeycloakAuthService(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }
    public String getAdminToken() {
        String tokenUrl = "http://localhost:8081/realms/master/protocol/openid-connect/token";

        String clientId = keycloakProperties.getAdminClient().getClientId();
        String clientSecret = keycloakProperties.getAdminClient().getClientSecret();
        String grantType = "client_credentials";

        // Corps de la requête (x-www-form-urlencoded)
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map responseBody = response.getBody();
            assert responseBody != null;
            return (String) responseBody.get("access_token");
        } else {
            throw new RuntimeException("Impossible d’obtenir le token d’accès de Keycloak");
        }
    }
}
