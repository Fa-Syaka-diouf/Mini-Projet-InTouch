package com.elfstack.toys.security.resetOtp;

import jakarta.mail.internet.MimeMessage;
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
import org.springframework.mail.javamail.MimeMessageHelper;
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
@CrossOrigin(origins = {"http://localhost:8081"},
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class DevResetOtp {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-client.client-secret}")
    private String secret_admin;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, ResetToken> resetTokens = new ConcurrentHashMap<>();

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
            List<CredentialRepresentation> otpCredentials = userResource.credentials().stream()
                    .filter(cred -> "otp".equals(cred.getType()))
                    .toList();

            if (otpCredentials.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Aucun OTP configuré pour ce compte"));
            }

            String resetToken = generateResetToken();
            resetTokens.put(resetToken, new ResetToken(userId, username, email));
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

    @GetMapping("/confirm-reset-otp")
    public ResponseEntity<?> confirmResetOTP(@RequestParam String token) {
        try {
            log.info("Confirmation de réinitialisation OTP avec token: {}", token);

            ResetToken resetToken = resetTokens.get(token);
            if (resetToken == null) {
                log.warn("Token invalide: {}", token);
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Lien de réinitialisation invalide"));
            }

            if (resetToken.isExpired()) {
                resetTokens.remove(token);
                log.warn("Token expiré: {}", token);
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Lien de réinitialisation expiré"));
            }

            log.info("Suppression de l'OTP pour l'utilisateur: {}", resetToken.getUsername());

            Keycloak keycloak = getKeycloakInstance();
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(resetToken.getUserId());

            // Suppression des OTP existants
            List<CredentialRepresentation> otpCredentials = userResource.credentials().stream()
                    .filter(cred -> "otp".equals(cred.getType()))
                    .collect(Collectors.toList());

            log.info("Nombre d'OTP trouvés à supprimer: {}", otpCredentials.size());

            for (CredentialRepresentation cred : otpCredentials) {
                log.info("Suppression de l'OTP avec ID: {}", cred.getId());
                userResource.removeCredential(cred.getId());
            }

            // Ajout de la Required Action
            UserRepresentation user = userResource.toRepresentation();
            if (user.getRequiredActions() == null) {
                user.setRequiredActions(new ArrayList<>());
            }

            user.getRequiredActions().clear(); // Nettoyer les autres actions
            user.getRequiredActions().add("CONFIGURE_TOTP");
            userResource.update(user);

            log.info("Required Action CONFIGURE_TOTP ajoutée pour: {}", resetToken.getUsername());

            // URL de redirection vers Keycloak
            String keycloakLoginUrl = keycloakServerUrl + "/realms/" + realm +
                    "/protocol/openid-connect/auth" +
                    "?client_id=task-management-app" +  // Remplacez par votre vrai client-id
                    "&response_type=code" +
                    "&scope=openid%20profile%20email" +
                    "&redirect_uri=" + java.net.URLEncoder.encode(frontendUrl + "/login/oauth2/code/keycloak", "UTF-8") +
                    "&login_hint=" + java.net.URLEncoder.encode(resetToken.getUsername(), "UTF-8");

            resetTokens.remove(token);

            log.info("Redirection vers: {}", keycloakLoginUrl);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(keycloakLoginUrl))
                    .build();

        } catch (Exception e) {
            log.error("Erreur lors de la confirmation de réinitialisation OTP", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Erreur interne du serveur"));
        }
    }

    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void sendResetEmail(String email, String username, String resetToken) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Réinitialisation de votre OTP - InTouchTask");

            // CORRECTION MAJEURE : URL vers l'endpoint API correct
            String confirmationUrl = frontendUrl + "/api/confirm-reset-otp?token=" + resetToken;

            String emailContent = "<html>" +
                    "<body>" +
                    "<p>Bonjour <b>" + username + "</b>,</p>" +
                    "<p>Vous avez demandé la réinitialisation de votre authentification à deux facteurs (OTP).</p>" +
                    "<p>Cliquez sur le lien suivant pour confirmer la réinitialisation :</p>" +
                    "<p><a href='" + confirmationUrl + "'>Réinitialiser mon OTP</a></p>" +
                    "<p>Ce lien expire dans 1 heure.</p>" +
                    "<p>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.</p>" +
                    "<br>" +
                    "<p>Cordialement,<br>L'équipe InTouchTask</p>" +
                    "</body>" +
                    "</html>";

            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);

            log.info("Email de réinitialisation OTP envoyé à {} avec URL: {}", email, confirmationUrl);

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