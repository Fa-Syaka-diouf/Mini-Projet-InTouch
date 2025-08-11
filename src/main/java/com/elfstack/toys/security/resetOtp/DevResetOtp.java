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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8081", "http://127.0.0.1:8081"},
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
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

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Autowired
    private JavaMailSender mailSender;

    // Stockage temporaire des tokens de réinitialisation
    // En production, utilisez Redis ou une base de données
    private final Map<String, ResetToken> resetTokens = new ConcurrentHashMap<>();

    // Classe interne pour gérer les tokens
    private static class ResetToken {
        private final String userId;
        private final String username;
        private final String email;
        private final LocalDateTime expiryTime;

        public ResetToken(String userId, String username, String email) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.expiryTime = LocalDateTime.now().plusHours(1);
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
    }

    /**
     * ÉTAPE 1: Demande de réinitialisation OTP
     * Cette méthode envoie seulement un email avec un lien de confirmation
     */
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

            Keycloak keycloak = getKeycloakInstance();
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.search(username);
            if (users.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Utilisateur non trouvé"));
            }

            UserRepresentation user = users.get(0);
            String userId = user.getId();

            if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email)) {
                log.warn("Tentative de réinitialisation OTP avec email incorrect - User: {}, Email fourni: {}",
                        username, email);
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "L'adresse email ne correspond pas à celle du compte"));
            }

            if (!Boolean.TRUE.equals(user.isEmailVerified())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "L'adresse email du compte n'est pas vérifiée"));
            }

            UserResource userResource = usersResource.get(userId);
            List<CredentialRepresentation> credentials = userResource.credentials();
            List<CredentialRepresentation> otpCredentials = credentials.stream()
                    .filter(cred -> "otp".equals(cred.getType()))
                    .toList();

            if (otpCredentials.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Aucun OTP configuré pour ce compte"));
            }

            // Génération du token de réinitialisation
            String resetToken = generateResetToken();

            // Stockage temporaire du token avec les informations utilisateur
            resetTokens.put(resetToken, new ResetToken(userId, username, email));

            // Envoi de l'email avec le lien de confirmation
            sendResetEmail(email, username, resetToken);

            log.info("Email de réinitialisation OTP envoyé pour l'utilisateur: {}, Email: {}", username, email);

            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "Email de réinitialisation envoyé"));

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation OTP", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Erreur interne du serveur"));
        }
    }

    /**
     * ÉTAPE 2: Confirmation de la réinitialisation OTP
     * Cette méthode est appelée quand l'utilisateur clique sur le lien dans l'email
     */
    @GetMapping("/confirm-reset-otp")
    public ResponseEntity<?> confirmResetOTP(@RequestParam String token) {
        try {
            // Vérification de l'existence du token
            ResetToken resetToken = resetTokens.get(token);
            if (resetToken == null) {
                log.warn("Tentative d'utilisation d'un token invalide: {}", token);
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Lien de réinitialisation invalide"));
            }

            // Vérification de l'expiration du token
            if (resetToken.isExpired()) {
                resetTokens.remove(token); // Nettoyer le token expiré
                log.warn("Tentative d'utilisation d'un token expiré: {}", token);
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Lien de réinitialisation expiré"));
            }

            // Connexion à Keycloak
            Keycloak keycloak = getKeycloakInstance();
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(resetToken.getUserId());

            // Suppression des anciens OTP
            List<CredentialRepresentation> credentials = userResource.credentials();
            List<CredentialRepresentation> otpCredentials = credentials.stream()
                    .filter(cred -> "otp".equals(cred.getType()))
                    .toList();

            // Supprimer tous les OTP existants
            otpCredentials.forEach(cred -> userResource.removeCredential(cred.getId()));

            // Supprimer le token utilisé
            resetTokens.remove(token);

            log.info("Réinitialisation OTP confirmée pour l'utilisateur: {}", resetToken.getUsername());

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl))
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de la confirmation de réinitialisation OTP", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Erreur interne du serveur"));
        }
    }

    /**
     * Génère un token de réinitialisation sécurisé
     */
    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void sendResetEmail(String email, String username, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Réinitialisation de votre OTP - InTouchTask");

            String confirmationUrl = frontendUrl + "/api/confirm-reset-otp?token=" + resetToken;

            String emailContent = String.format(
                    "Bonjour %s,\n\n" +
                            "Vous avez demandé la réinitialisation de votre authentification à deux facteurs (OTP).\n\n" +
                            "Cliquez sur le lien suivant pour confirmer la réinitialisation :\n" +
                            "%s\n\n" +
                            "Ce lien expire dans 1 heure.\n\n" +
                            "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe InTouchTask",
                    username,
                    confirmationUrl
            );

            message.setText(emailContent);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}", email, e);
            throw new RuntimeException("Impossible d'envoyer l'email", e);
        }
    }

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm("master")
                .clientId("admin-cli")
                .clientSecret(secret_admin)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        return email.matches(emailRegex);
    }
}