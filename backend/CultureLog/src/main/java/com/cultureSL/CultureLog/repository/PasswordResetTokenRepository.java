package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.PasswordResetToken;
import com.cultureSL.CultureLog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link PasswordResetToken}.
 * <p>
 * Gestiona los tokens de seguridad temporales utilizados en el flujo de recuperación de contraseña.
 * </p>
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token de restablecimiento por su cadena alfanumérica única.
     *
     * @param token Cadena del token (UUID).
     * @return Un {@link Optional} con la entidad del token si existe.
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Elimina todos los tokens asociados a un usuario específico.
     * Se utiliza para limpiar solicitudes antiguas o inválidas antes de generar una nueva.
     *
     * @param user Usuario propietario de los tokens.
     */
    void deleteByUser(User user);
}