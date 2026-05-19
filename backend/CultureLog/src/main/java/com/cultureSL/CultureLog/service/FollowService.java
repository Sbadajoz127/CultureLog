package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.dto.FollowRequestResponse;
import com.cultureSL.CultureLog.dto.UserSuggestionResponse;
import com.cultureSL.CultureLog.model.Follow;
import java.util.List;

/**
 * Servicio que gestiona las relaciones de seguimiento entre usuarios (grafo social).
 * <p>
 * Define el contrato para seguir/dejar de seguir usuarios y consultar
 * las listas de seguidores y seguidos.
 * </p>
 */
public interface FollowService {

    /**
     * Crea una relación de seguimiento entre dos usuarios.
     * <p>
     * Si el perfil del usuario destino es privado, la relación se crea con estado PENDING;
     * si es público, se acepta automáticamente (ACCEPTED).
     * Genera una notificación y, opcionalmente, un email al usuario seguido.
     * </p>
     *
     * @param followerId ID del usuario que inicia el seguimiento
     * @param followedId ID del usuario a seguir
     * @return el estado resultante de la relación ({@code ACCEPTED} o {@code PENDING})
     * @throws com.cultureSL.CultureLog.exception.BadRequestException       si el usuario intenta seguirse a sí mismo o ya lo sigue
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si alguno de los usuarios no existe
     */
    String followUser(Long followerId, Long followedId);

    /**
     * Elimina la relación de seguimiento entre dos usuarios.
     *
     * @param followerId ID del usuario seguidor
     * @param followedId ID del usuario seguido
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si la relación no existe
     */
    void unfollowUser(Long followerId, Long followedId);

    /**
     * Comprueba si un usuario sigue a otro.
     *
     * @param followerId ID del posible seguidor
     * @param followedId ID del posible seguido
     * @return {@code true} si existe la relación de seguimiento
     */
    boolean isFollowing(Long followerId, Long followedId);

    /**
     * Comprueba si dos usuarios se siguen mutuamente (ambos con estado ACCEPTED).
     * Utilizado para la privacidad {@code SOLO_AMIGOS}.
     *
     * @param userA ID del primer usuario
     * @param userB ID del segundo usuario
     * @return {@code true} si ambos se siguen mutuamente
     */
    boolean isMutualFollow(Long userA, Long userB);

    /**
     * Obtiene la lista de seguidores de un usuario (relaciones con estado ACCEPTED).
     *
     * @param userId ID del usuario
     * @return lista de relaciones de seguimiento donde el usuario es seguido
     */
    List<Follow> getFollowers(Long userId);

    /**
     * Obtiene directamente los IDs de los seguidores de un usuario (estado ACCEPTED).
     * Más eficiente que getFollowers() cuando solo se necesitan los IDs.
     *
     * @param userId ID del usuario
     * @return lista de IDs de los seguidores
     */
    List<Long> getFollowerIds(Long userId);

    /**
     * Obtiene la lista de usuarios a los que sigue un usuario (relaciones con estado ACCEPTED).
     *
     * @param userId ID del usuario
     * @return lista de relaciones de seguimiento donde el usuario es seguidor
     */
    List<Follow> getFollowing(Long userId);

    /**
     * Acepta una solicitud de seguimiento pendiente.
     * <p>
     * Cambia el estado de la relación de PENDING a ACCEPTED y genera una notificación
     * al usuario que envió la solicitud.
     * </p>
     *
     * @param followedId ID del usuario que acepta (el que recibió la solicitud)
     * @param followerId ID del usuario que envió la solicitud
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si no existe solicitud pendiente
     */
    void acceptFollowRequest(Long followedId, Long followerId);

    /**
     * Rechaza una solicitud de seguimiento pendiente eliminando la relación.
     * <p>
     * La relación se elimina de la base de datos, permitiendo al usuario
     * enviar una nueva solicitud en el futuro.
     * </p>
     *
     * @param followedId ID del usuario que rechaza (el que recibió la solicitud)
     * @param followerId ID del usuario que envió la solicitud
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si no existe solicitud pendiente
     */
    void rejectFollowRequest(Long followedId, Long followerId);

    /**
     * Obtiene las solicitudes de seguimiento pendientes recibidas por un usuario.
     *
     * @param userId ID del usuario que recibió las solicitudes
     * @return lista de solicitudes pendientes mapeadas a DTO
     */
    List<FollowRequestResponse> getPendingRequests(Long userId);

    /**
     * Cuenta las solicitudes de seguimiento pendientes recibidas por un usuario.
     *
     * @param userId ID del usuario que recibió las solicitudes
     * @return número de solicitudes pendientes
     */
    long getPendingRequestsCount(Long userId);

    /**
     * Obtiene la lista de seguidores de un usuario en formato ligero.
     * Verifica que el visor tenga acceso según la privacidad del perfil consultado.
     *
     * @param userId   ID del usuario consultado
     * @param viewerId ID del usuario que solicita la lista
     * @return lista de usuarios seguidores
     * @throws com.cultureSL.CultureLog.exception.UnauthorizedException si el visor no tiene acceso
     */
    List<UserSuggestionResponse> getFollowersSummary(Long userId, Long viewerId);

    /**
     * Obtiene la lista de seguidos de un usuario en formato ligero.
     * Verifica que el visor tenga acceso según la privacidad del perfil consultado.
     *
     * @param userId   ID del usuario consultado
     * @param viewerId ID del usuario que solicita la lista
     * @return lista de usuarios seguidos
     * @throws com.cultureSL.CultureLog.exception.UnauthorizedException si el visor no tiene acceso
     */
    List<UserSuggestionResponse> getFollowingSummary(Long userId, Long viewerId);
}
