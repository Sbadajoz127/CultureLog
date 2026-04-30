package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Token de seguridad temporal para la verificación de correo electrónico en el registro.
 * <p>
 * Vincula un código único (UUID) a un usuario por un tiempo limitado (24 horas),
 * permitiendo validar que el correo proporcionado durante el registro es legítimo.
 * </p>
 */
@Entity
@Table(name = "email_verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Código único generado para la validación. */
    @Column(unique = true)
    private String token;

    /** Usuario propietario del token. */
    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    /** Fecha y hora en la que el token dejará de ser válido. */
    private LocalDateTime expiryDate;

    /**
     * Constructor que genera automáticamente el token y establece la expiración en 24h.
     * @param user Usuario que solicita la verificación.
     */
    public EmailVerificationToken(User user) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = LocalDateTime.now().plusHours(24);
    }

    /**
     * Verifica si el token ha caducado.
     * @return true si la fecha actual es posterior a la de expiración.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
