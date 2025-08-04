package com.elfstack.toys.security.resetOtp;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/api")
public class DevResetOtp {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;
    @Value("${keycloak.admin-client.client-secret}")
    private String secret_admin;

    @PostMapping("/reset-otp")
    public ResponseEntity<?> resetOTP(@RequestParam String username,
                                      @RequestParam String email) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Username requis"));
            }

            if (email == null || !isValidEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email valide requis"));
            }
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm("master")
                    .clientId("admin-cli")
                    .clientSecret(secret_admin)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .build();

            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.search(username);
            if (users.isEmpty()) {
                return ResponseEntity.badRequest().body("Utilisateur non trouve");
            }

            UserRepresentation user = users.getFirst();
            String userId = user.getId();
            UserResource userResource = usersResource.get(userId);

            if (user.getEmail() == null ||
                    !user.getEmail().equalsIgnoreCase(email)) {
                log.warn("Tentative de réinitialisation OTP avec email incorrect - User: {}, Email fourni: {}",
                        username, email);
                return ResponseEntity.badRequest().body("L'adresse email ne correspond pas à celle du compte");
            }

            List<CredentialRepresentation> credentials = userResource.credentials();
            List<CredentialRepresentation> otpCredentials = credentials.stream()
                    .filter(cred -> "otp".equals(cred.getType()))
                    .toList();

            if (otpCredentials.isEmpty()) {
                return ResponseEntity.badRequest().body("Aucun OTP configuré pour ce compte");
            }

            if (!Boolean.TRUE.equals(user.isEmailVerified())) {
                return ResponseEntity.badRequest().body("L'adresse email du compte n'est pas vérifiée");
            }
            otpCredentials.forEach(cred -> userResource.removeCredential(cred.getId()));

            userResource.executeActionsEmail(List.of("CONFIGURE_TOTP"));

            log.info("Réinitialisation OTP réussie pour l'utilisateur: {}, Email: {}",
                    username, email);

            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "Email de réinitialisation envoyé"));

        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation OTP", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Erreur interne du serveur"));
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        return email.matches(emailRegex);
    }
}