package com.elfstack.toys.usermanagement.service;

import com.elfstack.toys.usermanagement.domain.KeycloakUserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
        ResponseEntity<KeycloakUserDto[]> response = restTemplate.getForEntity(baseUrl + "/users", KeycloakUserDto[].class);
        KeycloakUserDto[] users = response.getBody();
        return users != null ? Arrays.asList(users) : List.of();
    }

    public List<String> getAllUsernames() {
        return getAllUsers().stream()
                .map(KeycloakUserDto::getUsername)
                .collect(Collectors.toList());
    }

}
