package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import com.cultureSL.CultureLog.model.enums.FollowStatus;

/**
 * Representa una relación de seguimiento entre dos usuarios (Grafo Social).
 * <p>
 * Esta entidad gestiona quién sigue a quién y el estado de dicha solicitud.
 * Incluye una restricción única para evitar que un usuario siga dos veces a la misma persona.
 * </p>
 */
@Entity
@Table(name = "follows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "followed_id"})
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Follow {

    /** Identificador único de la relación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * El usuario que inicia la acción de seguir (Seguidor).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    /**
     * El usuario que recibe la acción (Seguido).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;

    /**
     * Estado actual de la relación.
     * <ul>
     * <li>PENDING: Si el perfil destino es privado y requiere aprobación.</li>
     * <li>ACCEPTED: Si la relación está activa y validada.</li>
     * </ul>
     */
    @Enumerated(EnumType.STRING)
    private FollowStatus status;

    /** Fecha en la que se inició la solicitud de seguimiento. */
    @CreationTimestamp
    private LocalDateTime createdAt;
}