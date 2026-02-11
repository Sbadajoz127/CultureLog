package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Notification}.
 * <p>
 * Gestiona las alertas internas de la aplicación, permitiendo recuperar el historial
 * y contar las notificaciones pendientes de lectura.
 * </p>
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Obtiene el historial de notificaciones de un usuario de forma paginada.
     * Los resultados se ordenan para mostrar primero las notificaciones más recientes.
     *
     * @param recipientId ID del usuario receptor.
     * @param pageable    Objeto de paginación (número de página y tamaño).
     * @return Página de notificaciones ordenadas por fecha descendente.
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    /**
     * Cuenta cuántas notificaciones tiene el usuario marcadas como no leídas.
     * Se utiliza para mostrar el indicador (badge) rojo en la interfaz de usuario.
     *
     * @param recipientId ID del usuario receptor.
     * @return Número total de notificaciones sin leer.
     */
    long countByRecipientIdAndIsReadFalse(Long recipientId);
}