package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.Follow;
import com.cultureSL.CultureLog.model.enums.FollowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link Follow}.
 * <p>
 * Gestiona las relaciones del grafo social entre usuarios (seguidores y seguidos),
 * permitiendo consultar el estado de estas conexiones.
 * </p>
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /**
     * Verifica si existe una relación de seguimiento entre dos usuarios, sin importar el estado.
     *
     * @param followerId ID del usuario que sigue.
     * @param followedId ID del usuario que es seguido.
     * @return {@code true} si existe la relación, {@code false} en caso contrario.
     */
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    /**
     * Verifica si existe una relación de seguimiento entre dos usuarios con un estado concreto.
     *
     * @param followerId ID del usuario que sigue.
     * @param followedId ID del usuario que es seguido.
     * @param status     Estado requerido de la relación.
     * @return {@code true} si existe la relación con ese estado.
     */
    boolean existsByFollowerIdAndFollowedIdAndStatus(Long followerId, Long followedId, FollowStatus status);

    /**
     * Busca la entidad de relación específica entre dos usuarios.
     * Útil para recuperar el objeto antes de eliminarlo (dejar de seguir) o cambiar su estado.
     *
     * @param followerId ID del seguidor.
     * @param followedId ID del seguido.
     * @return Un {@link Optional} que contiene la relación si existe.
     */
    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    /**
     * Obtiene la lista de personas a las que sigue un usuario, filtrada por estado.
     * Ej: "Mis amigos" (Status ACCEPTED) o "Solicitudes enviadas" (Status PENDING).
     *
     * @param followerId ID del usuario origen.
     * @param status     Estado de la relación (ej. ACCEPTED).
     * @return Lista de relaciones donde el usuario actúa como seguidor.
     */
    List<Follow> findByFollowerIdAndStatus(Long followerId, FollowStatus status);

    /**
     * Obtiene la lista de personas que siguen a un usuario, filtrada por estado.
     * Ej: "Mis seguidores" (Status ACCEPTED) o "Solicitudes recibidas" (Status PENDING).
     *
     * @param followedId ID del usuario destino.
     * @param status     Estado de la relación.
     * @return Lista de relaciones donde el usuario actúa como seguido.
     */
    List<Follow> findByFollowedIdAndStatus(Long followedId, FollowStatus status);

    /**
     * Verifica si existe una relación de seguimiento entre dos usuarios excluyendo un estado concreto.
     * Útil para impedir que un usuario bloqueado pueda re-seguir.
     *
     * @param followerId ID del usuario que sigue.
     * @param followedId ID del usuario que es seguido.
     * @param status     Estado a excluir de la búsqueda.
     * @return {@code true} si existe una relación con estado distinto al indicado.
     */
    boolean existsByFollowerIdAndFollowedIdAndStatusNot(Long followerId, Long followedId, FollowStatus status);

    /**
     * Busca una relación de seguimiento específica filtrando por estado.
     * Útil para gestionar solicitudes pendientes (aceptar/rechazar).
     *
     * @param followerId ID del seguidor.
     * @param followedId ID del seguido.
     * @param status     Estado requerido de la relación.
     * @return Un {@link Optional} con la relación si existe con ese estado.
     */
    Optional<Follow> findByFollowerIdAndFollowedIdAndStatus(Long followerId, Long followedId, FollowStatus status);

    /**
     * Obtiene directamente los IDs de los seguidores de un usuario con un estado dado.
     * Evita cargar entidades Follow completas y el problema N+1 al acceder a follower.
     *
     * @param followedId ID del usuario seguido.
     * @param status     Estado requerido de la relación.
     * @return Lista de IDs de los seguidores.
     */
    @Query("SELECT f.follower.id FROM Follow f WHERE f.followed.id = :followedId AND f.status = :status")
    List<Long> findFollowerIdsByFollowedIdAndStatus(@Param("followedId") Long followedId, @Param("status") FollowStatus status);

    long countByFollowedIdAndStatus(Long followedId, FollowStatus status);

    long countByFollowerIdAndStatus(Long followerId, FollowStatus status);
}