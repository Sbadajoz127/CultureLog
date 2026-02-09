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

/**
 * Implementación del servicio de envío de correos electrónicos.
 * <p>
 * Esta clase utiliza {@link JavaMailSender} para enviar correos en formato HTML de manera asíncrona,
 * asegurando que el proceso de envío no bloquee el hilo principal de la aplicación.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * Envía un correo electrónico genérico con contenido HTML.
     * <p>Este método se ejecuta en un hilo separado (Async).</p>
     *
     * @param to       Dirección de correo del destinatario.
     * @param subject  Asunto del correo.
     * @param htmlBody Cuerpo del mensaje en formato HTML.
     */
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

    /**
     * Envía una notificación por correo a un usuario cuando consigue un nuevo seguidor.
     * <p>Genera una plantilla HTML visual con el nombre del nuevo seguidor.</p>
     *
     * @param toEmail          Correo del usuario que es seguido.
     * @param followerUsername Nombre de usuario de la persona que ha comenzado a seguir.
     */
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

    /**
     * Envía un correo con el token de seguridad para restablecer la contraseña.
     *
     * @param to    Correo del usuario que solicitó el cambio.
     * @param token El token único generado (UUID) para validar la operación.
     */
    @Override
    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Restablecer contraseña - CultureLog";
        
        // Envio del token para copiar/pegar
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #D32F2F;">Recuperación de Contraseña</h2>
                <p>Hola,</p>
                <p>Hemos recibido una solicitud para restablecer tu contraseña.</p>
                <p>Copia el siguiente código de seguridad en la aplicación:</p>
                <div style="background-color: #f5f5f5; padding: 15px; text-align: center; border-radius: 5px;">
                    <h1 style="margin: 0; letter-spacing: 2px; color: #333;">%s</h1>
                </div>
                <p>Este código expirará en 24 horas.</p>
                <p style="font-size: 12px; color: #999;">Si no solicitaste este cambio, ignora este correo.</p>
            </div>
            """.formatted(token);

        sendHtmlEmail(to, subject, htmlContent);
    }
}