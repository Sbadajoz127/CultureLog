package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.model.User;
import java.util.Optional;

/**
 * Servicio que gestiona las operaciones relacionadas con los usuarios.
 * <p>
 * Define el contrato para registro, autenticación, gestión de perfil
 * y recuperación de contraseña.
 * </p>
 */
public interface UserService {

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Cifra la contraseña, verifica que no existan duplicados de username/email
     * y crea la configuración por defecto ({@link com.cultureSL.CultureLog.model.UserSettings}).
     * </p>
     *
     * @param user entidad con los datos del nuevo usuario
     * @return el usuario persistido con su ID generado
     * @throws Exception si el username o email ya están registrados
     */
    User registerUser(User user) throws Exception;

    /**
     * Autentica a un usuario mediante sus credenciales.
     *
     * @param username    nombre de usuario
     * @param rawPassword contraseña en texto plano
     * @return un {@link Optional} con el usuario si las credenciales son válidas, vacío en caso contrario
     */
    Optional<User> login(String username, String rawPassword);

    /**
     * Comprueba si existe un usuario con el nombre de usuario dado.
     *
     * @param username nombre de usuario a verificar
     * @return {@code true} si ya existe un usuario con ese username
     */
    boolean exists(String username);

    /**
     * Actualiza la foto de perfil de un usuario.
     *
     * @param userId   ID del usuario
     * @param imageUrl nueva URL de la imagen de perfil
     */
    void updateProfilePicture(Long userId, String imageUrl);

    /**
     * Elimina la foto de perfil de un usuario, dejándola como {@code null}.
     *
     * @param userId ID del usuario
     */
    void removeProfilePicture(Long userId);

    /**
     * Inicia el flujo de recuperación de contraseña enviando un email con un token temporal.
     *
     * @param email dirección de correo del usuario
     * @throws Exception si el email no está asociado a ningún usuario
     */
    void requestPasswordReset(String email) throws Exception;

    /**
     * Restablece la contraseña de un usuario utilizando un token de seguridad válido.
     *
     * @param token       token de restablecimiento (UUID)
     * @param newPassword nueva contraseña en texto plano (se cifrará antes de almacenar)
     * @throws Exception si el token es inválido o ha expirado
     */
    void resetPassword(String token, String newPassword) throws Exception;
}
