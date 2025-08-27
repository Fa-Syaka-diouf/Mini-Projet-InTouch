package com.elfstack.toys.security.resetOtp;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class EmailService {
    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;
    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendResetEmail(String email, String username, String resetToken) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Réinitialisation de votre OTP - InTouchTask");
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
}
