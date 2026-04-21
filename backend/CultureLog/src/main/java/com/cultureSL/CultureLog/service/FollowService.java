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
     * @throws com.cultureSL.CultureLog.exception.BadRequestException       si el usuario intenta seguirse a sí mismo o ya lo sigue
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si alguno de los usuarios no existe
     */
    void followUser(Long followerId, Long followedId);

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
     * Rechaza una solicitud de seguimiento pendiente, actualizando su estado a REJECTED.
     * <p>
     * La relación se conserva en la base de datos para impedir que el usuario
     * vuelva a enviar una solicitud.
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
     * Obtiene la lista de seguidores de un usuario en formato ligero.
     *
     * @param userId ID del usuario consultado
     * @return lista de usuarios seguidores
     */
    List<UserSuggestionResponse> getFollowersSummary(Long userId);

    /**
     * Obtiene la lista de seguidos de un usuario en formato ligero.
     *
     * @param userId ID del usuario consultado
     * @return lista de usuarios seguidos
     */
    List<UserSuggestionResponse> getFollowingSummary(Long userId);
}
