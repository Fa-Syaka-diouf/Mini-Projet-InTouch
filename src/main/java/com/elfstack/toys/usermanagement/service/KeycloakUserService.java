package com.elfstack.toys.usermanagement.service;

import com.elfstack.toys.usermanagement.domain.KeycloakProperties;
import com.elfstack.toys.usermanagement.domain.KeycloakUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Slf4j
public class KeycloakUserService {

        private final KeycloakAuthService keycloakAuthService;
        private final KeycloakProperties keycloakProperties;
        private final WebClient webClient;

        public KeycloakUserService(KeycloakAuthService keycloakAuthService, KeycloakProperties keycloakProperties) {
            this.keycloakAuthService = keycloakAuthService;
            this.keycloakProperties = keycloakProperties;
            this.webClient = WebClient.builder()
                    .baseUrl(keycloakProperties.getAuthServerUrl())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }

    public List<KeycloakUserDto> getAllUsers() {
        String realm = keycloakProperties.getRealm();
        String accessToken = keycloakAuthService.getAdminToken();

        return webClient.get()
                .uri("/admin/realms/{realm}/users", realm)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToFlux(KeycloakUserDto.class)
                .collectList()
                .block();
    }
}