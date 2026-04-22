package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.dto.UserProfileResponse;
import com.cultureSL.CultureLog.dto.UserSettingsRequest;
import com.cultureSL.CultureLog.dto.UserSuggestionResponse;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.model.UserSettings;

import java.util.List;
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
    User registerUser(User user);

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
    void requestPasswordReset(String email);

    /**
     * Restablece la contraseña de un usuario utilizando un token de seguridad válido.
     *
     * @param token       token de restablecimiento (UUID)
     * @param newPassword nueva contraseña en texto plano (se cifrará antes de almacenar)
     * @throws Exception si el token es inválido o ha expirado
     */
    void resetPassword(String token, String newPassword);

    UserSettings updateSettings(Long userId, UserSettingsRequest request);

    UserSettings getSettings(Long userId);

    /**
     * Devuelve una lista de usuarios sugeridos para seguir.
     *
     * @param userId ID del usuario autenticado
     * @return lista de sugerencias con datos básicos (id, username, foto)
     */
    List<UserSuggestionResponse> getSuggestedUsers(Long userId);

    /**
     * Obtiene el perfil público de un usuario por username, respetando la configuración de privacidad.
     *
     * @param username     nombre de usuario cuyo perfil se consulta
     * @param viewerUserId ID del usuario que visualiza el perfil
     * @return DTO con stats, estado de follow, y contenido según permisos
     */
    UserProfileResponse getUserProfileByUsername(String username, Long viewerUserId);

    /**
     * Solicita la eliminación de la cuenta del usuario, enviando un código de confirmación por email.
     *
     * @param userId ID del usuario que solicita la eliminación
     */
    void requestAccountDeletion(Long userId);

    /**
     * Confirma la eliminación de la cuenta usando el código recibido por email.
     *
     * @param userId ID del usuario
     * @param code   código de 6 dígitos recibido por email
     */
    void confirmAccountDeletion(Long userId, String code);

    /**
     * Actualiza el banner de perfil de un usuario.
     *
     * @param userId   ID del usuario
     * @param imageUrl nueva URL de la imagen del banner
     */
    void updateBanner(Long userId, String imageUrl);

    /**
     * Elimina el banner de perfil de un usuario, dejándolo como {@code null}.
     *
     * @param userId ID del usuario
     */
    void removeBanner(Long userId);

    /**
     * Busca usuarios por nombre de usuario.
     *
     * @param query  texto a buscar
     * @param userId ID del usuario que realiza la búsqueda (se excluye de resultados)
     * @return lista de usuarios encontrados
     */
    List<UserSuggestionResponse> searchUsers(String query, Long userId);
}
