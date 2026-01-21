package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Email enviado correctamente a {}", to);

        } catch (MessagingException e) {
            log.error("Error al enviar email a {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendNewFollowerNotification(String toEmail, String followerUsername) {
        String subject = "¡Tienes un nuevo seguidor en CultureLog!";
        
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #448AFF;">¡Alguien te está siguiendo!</h2>
                <p>Hola,</p>
                <p>El usuario <strong>%s</strong> ha comenzado a seguirte.</p>
                <p>Entra en la app para ver su perfil.</p>
                <br>
                <p style="font-size: 12px; color: #999;">El equipo de CultureLog</p>
            </div>
            """.formatted(followerUsername);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }
}