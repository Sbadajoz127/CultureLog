package com.cultureSL.CultureLog.service;

/**
 * Servicio de envío de correos electrónicos.
 * <p>
 * Define el contrato para el envío de emails HTML, incluyendo notificaciones
 * de nuevos seguidores y enlaces de recuperación de contraseña.
 * </p>
 */
public interface EmailService {

    /**
     * Envía un correo electrónico con contenido HTML.
     *
     * @param to      dirección de correo del destinatario
     * @param subject asunto del mensaje
     * @param htmlBody cuerpo del mensaje en formato HTML
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);

    /**
     * Envía una notificación por email informando de un nuevo seguidor.
     *
     * @param toEmail          dirección de correo del usuario que recibe el seguimiento
     * @param followerUsername nombre de usuario del nuevo seguidor
     */
    void sendNewFollowerNotification(String toEmail, String followerUsername);

    /**
     * Envía una notificación por email informando de un like en una publicación.
     *
     * @param toEmail       dirección de correo del autor del post
     * @param likerUsername nombre de usuario que dio like
     */
    void sendLikeNotification(String toEmail, String likerUsername);

    /**
     * Envía una notificación por email informando de un comentario en una publicación.
     *
     * @param toEmail            dirección de correo del autor del post
     * @param commenterUsername  nombre de usuario que comentó
     */
    void sendCommentNotification(String toEmail, String commenterUsername);

    /**
     * Envía un email con el enlace para restablecer la contraseña.
     *
     * @param to    dirección de correo del usuario
     * @param token token de seguridad (UUID) para validar la solicitud
     */
    void sendPasswordResetEmail(String to, String token);
}
