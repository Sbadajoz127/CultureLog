package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link User}.
 * <p>
 * Proporciona métodos para la autenticación y validación de usuarios,
 * permitiendo búsquedas por nombre de usuario y correo electrónico.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca un usuario por su nombre de usuario (username).
     * Utilizado principalmente durante el proceso de login.
     *
     * @param username Nombre de usuario.
     * @return Un {@link Optional} con el usuario si existe.
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su correo electrónico.
     * Utilizado para la recuperación de contraseñas o login alternativo.
     *
     * @param email Correo electrónico.
     * @return Un {@link Optional} con el usuario si existe.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica si ya existe un usuario registrado con ese nombre.
     * Utilizado durante el registro para evitar duplicados.
     *
     * @param username Nombre de usuario a comprobar.
     * @return {@code true} si ya existe.
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica si ya existe un usuario registrado con ese correo electrónico.
     * Utilizado durante el registro para evitar duplicados.
     *
     * @param email Correo electrónico a comprobar.
     * @return {@code true} si ya existe.
     */
    boolean existsByEmail(String email);

    /**
     * Devuelve usuarios que el usuario actual no sigue (sin relación en follows),
     * excluyéndose a sí mismo. Útil para el widget de sugerencias.
     *
     * @param userId   ID del usuario autenticado.
     * @param pageable configuración de paginación para limitar resultados.
     * @return lista de usuarios sugeridos.
     */
    @Query("SELECT u FROM User u WHERE u.id <> :userId AND u.id NOT IN " +
           "(SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId)")
    List<User> findSuggestedUsers(@Param("userId") Long userId, Pageable pageable);
}