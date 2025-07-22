package com.elfstack.toys.usermanagement.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    private String AuthServerUrl;
    private String realm;
    private AdminClient adminClient;

    @Data
    public static class AdminClient {
        private String clientId;
        private String clientSecret;
    }
}

