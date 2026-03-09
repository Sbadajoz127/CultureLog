package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.cultureSL.CultureLog.model.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * Representa una alerta o notificación dentro de la aplicación.
 * <p>
 * Se genera cuando ocurren eventos sociales (likes, follows, comentarios) para informar
 * al usuario destinatario.
 * </p>
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {

    /** Identificador único de la notificación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Usuario que recibe la notificación. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /** Usuario que provocó el evento (quien dio like, quien comentó, etc.). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    /** Tipo de evento (NUEVO_SEGUIDOR, LIKE_POST, etc.). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /**
     * ID de referencia a la entidad relacionada con la notificación.
     * Por ejemplo, si es un LIKE, guarda el ID del Post para poder navegar a él al hacer clic.
     */
    private Long referenceId; 

    /** Indica si el usuario ya ha visto esta notificación. */
    private boolean isRead = false;

    /** Fecha y hora de creación de la notificación (generada automáticamente). */
    @CreationTimestamp
    private LocalDateTime createdAt;
}