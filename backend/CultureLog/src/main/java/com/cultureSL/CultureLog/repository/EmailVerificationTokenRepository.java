package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.EmailVerificationToken;
import com.cultureSL.CultureLog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link EmailVerificationToken}.
 * <p>
 * Gestiona los tokens de seguridad temporales utilizados en el flujo de verificación
 * de correo electrónico durante el registro.
 * </p>
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    /**
     * Busca un token de verificación por su cadena alfanumérica única.
     *
     * @param token Cadena del token (UUID).
     * @return Un {@link Optional} con la entidad del token si existe.
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * Elimina todos los tokens asociados a un usuario específico.
     * Se utiliza para limpiar tokens antiguos antes de generar uno nuevo.
     *
     * @param user Usuario propietario de los tokens.
     */
    void deleteByUser(User user);
}
