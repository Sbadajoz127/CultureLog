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

    @Override
    @Async
    public void sendLikeNotification(String toEmail, String likerUsername) {
        String subject = "A alguien le gusta tu publicación en CultureLog";

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #448AFF;">&#10084; ¡Nuevo Me Gusta!</h2>
                <p>Hola,</p>
                <p>El usuario <strong>%s</strong> le ha dado Me Gusta a tu publicación.</p>
                <p>Entra en la app para verlo.</p>
                <br>
                <p style="font-size: 12px; color: #999;">El equipo de CultureLog</p>
            </div>
            """.formatted(likerUsername);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Override
    @Async
    public void sendCommentNotification(String toEmail, String commenterUsername) {
        String subject = "Nuevo comentario en tu publicación de CultureLog";

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #448AFF;">&#128172; ¡Nuevo Comentario!</h2>
                <p>Hola,</p>
                <p>El usuario <strong>%s</strong> ha comentado en tu publicación.</p>
                <p>Entra en la app para responder.</p>
                <br>
                <p style="font-size: 12px; color: #999;">El equipo de CultureLog</p>
            </div>
            """.formatted(commenterUsername);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Envía un correo con el código de verificación para activar la cuenta recién registrada.
     *
     * @param to    Correo del usuario que se acaba de registrar.
     * @param token El token único generado (UUID) para validar el correo.
     */
    @Override
    @Async
    public void sendEmailVerificationCode(String to, String token) {
        String subject = "Verifica tu cuenta - CultureLog";

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #448AFF;">¡Bienvenido a CultureLog!</h2>
                <p>Hola,</p>
                <p>Gracias por registrarte. Para activar tu cuenta, copia el siguiente código de verificación en la aplicación:</p>
                <div style="background-color: #f5f5f5; padding: 15px; text-align: center; border-radius: 5px;">
                    <h1 style="margin: 0; letter-spacing: 2px; color: #333;">%s</h1>
                </div>
                <p>Este código expirará en 24 horas.</p>
                <p style="font-size: 12px; color: #999;">Si no creaste esta cuenta, ignora este correo.</p>
                <p style="font-size: 12px; color: #999;">El equipo de CultureLog</p>
            </div>
            """.formatted(token);

        sendHtmlEmail(to, subject, htmlContent);
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

    /**
     * Envía un correo con el código de confirmación para eliminar la cuenta.
     *
     * @param to   Correo del usuario que solicita la eliminación.
     * @param code Código de 6 dígitos para confirmar la eliminación.
     */
    @Override
    @Async
    public void sendAccountDeletionCode(String to, String code) {
        String subject = "Código de confirmación para eliminar tu cuenta - CultureLog";

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #D32F2F;">&#9888; Eliminación de Cuenta</h2>
                <p>Hola,</p>
                <p>Hemos recibido una solicitud para <strong>eliminar permanentemente</strong> tu cuenta de CultureLog.</p>
                <p>Si deseas continuar, introduce el siguiente código en la aplicación:</p>
                <div style="background-color: #ffebee; padding: 20px; text-align: center; border-radius: 5px; border: 2px solid #D32F2F;">
                    <h1 style="margin: 0; letter-spacing: 4px; color: #D32F2F; font-size: 32px;">%s</h1>
                </div>
                <p style="margin-top: 15px;"><strong>Este código expirará en 15 minutos.</strong></p>
                <p style="color: #D32F2F;"><strong>Advertencia:</strong> Esta acción es irreversible. Se eliminarán todos tus datos, publicaciones, comentarios y biblioteca.</p>
                <br>
                <p style="font-size: 12px; color: #999;">Si no solicitaste eliminar tu cuenta, ignora este correo y tu cuenta permanecerá segura.</p>
                <p style="font-size: 12px; color: #999;">El equipo de CultureLog</p>
            </div>
            """.formatted(code);

        sendHtmlEmail(to, subject, htmlContent);
    }
}