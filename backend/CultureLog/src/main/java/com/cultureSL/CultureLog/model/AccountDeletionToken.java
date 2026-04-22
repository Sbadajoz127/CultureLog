package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Token de seguridad temporal para la eliminación de cuentas.
 * <p>
 * Vincula un código numérico de 6 dígitos a un usuario por un tiempo limitado (15 minutos),
 * permitiendo validar que la solicitud de eliminación de cuenta es legítima.
 * </p>
 */
@Entity
@Table(name = "account_deletion_tokens")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AccountDeletionToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Código numérico de 6 dígitos para la validación. */
    @Column(nullable = false)
    private String code;

    /** Usuario propietario del token. */
    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id", unique = true)
    private User user;

    /** Fecha y hora en la que el token dejará de ser válido. */
    private LocalDateTime expiryDate;

    /**
     * Constructor que genera automáticamente el código de 6 dígitos y establece la expiración en 15 minutos.
     * @param user Usuario que solicita la eliminación.
     */
    public AccountDeletionToken(User user) {
        this.user = user;
        this.code = generateSixDigitCode();
        this.expiryDate = LocalDateTime.now().plusMinutes(15);
    }

    /**
     * Genera un código numérico aleatorio de 6 dígitos.
     * @return código de 6 dígitos como String.
     */
    private String generateSixDigitCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Verifica si el token ha caducado.
     * @return true si la fecha actual es posterior a la de expiración.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
