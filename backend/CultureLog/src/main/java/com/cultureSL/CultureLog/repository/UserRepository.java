package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}