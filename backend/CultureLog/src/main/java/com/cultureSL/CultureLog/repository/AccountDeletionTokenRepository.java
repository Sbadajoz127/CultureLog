package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.AccountDeletionToken;
import com.cultureSL.CultureLog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link AccountDeletionToken}.
 * <p>
 * Gestiona los tokens de seguridad temporales utilizados en el flujo de eliminación de cuentas.
 * </p>
 */
@Repository
public interface AccountDeletionTokenRepository extends JpaRepository<AccountDeletionToken, Long> {

    /**
     * Busca un token de eliminación por su código numérico y usuario.
     *
     * @param code Código de 6 dígitos.
     * @param user Usuario propietario del token.
     * @return Un {@link Optional} con la entidad del token si existe.
     */
    Optional<AccountDeletionToken> findByCodeAndUser(String code, User user);

    /**
     * Busca un token de eliminación por usuario.
     *
     * @param user Usuario propietario del token.
     * @return Un {@link Optional} con la entidad del token si existe.
     */
    Optional<AccountDeletionToken> findByUser(User user);

    /**
     * Elimina todos los tokens asociados a un usuario específico.
     *
     * @param user Usuario propietario de los tokens.
     */
    void deleteByUser(User user);
}
