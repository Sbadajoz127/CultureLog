package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.model.Follow;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.model.enums.FollowStatus;
import com.cultureSL.CultureLog.model.enums.NotificationType;
import com.cultureSL.CultureLog.model.enums.ProfilePrivacy;
import com.cultureSL.CultureLog.repository.FollowRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.EmailService;
import com.cultureSL.CultureLog.service.FollowService;
import com.cultureSL.CultureLog.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de la lógica de negocio relacionada con el grafo social (Seguidores/Seguidos).
 * <p>
 * Gestiona la creación y eliminación de relaciones de seguimiento, verificando la privacidad
 * del perfil destino (Público vs Privado) y orquestando las notificaciones (Email e In-App).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    /**
     * Crea una relación de seguimiento entre dos usuarios.
     * <p>
     * Realiza las siguientes validaciones y acciones:
     * <ul>
     * <li>Verifica que el usuario no se siga a sí mismo.</li>
     * <li>Verifica que la relación no exista previamente.</li>
     * <li>Comprueba la privacidad del usuario destino:
     * <ul>
     * <li>Si es {@code PRIVADO}: El estado se guarda como {@code PENDING}.</li>
     * <li>Si es {@code PUBLICO}: El estado se guarda como {@code ACCEPTED}.</li>
     * </ul>
     * </li>
     * <li>Envía notificación por correo (si el usuario destino las tiene activadas).</li>
     * <li>Crea una notificación interna en la aplicación.</li>
     * </ul>
     *
     * @param followerId ID del usuario que quiere seguir (seguidor).
     * @param followedId ID del usuario a seguir.
     * @throws RuntimeException Si el usuario intenta seguirse a sí mismo o ya existe la relación.
     */
    @Override
    @Transactional
    public void followUser(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new RuntimeException("No puedes seguirte a ti mismo");
        }

        if (followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            throw new RuntimeException("Ya sigues a este usuario");
        }

        User follower = userRepository.findById(followerId).orElseThrow();
        User followed = userRepository.findById(followedId).orElseThrow();

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);

        if (followed.getSettings().getProfilePrivacy() == ProfilePrivacy.PRIVADO) {
            follow.setStatus(FollowStatus.PENDING);
        } else {
            follow.setStatus(FollowStatus.ACCEPTED);
        }

        followRepository.save(follow);

        if (followed.getSettings().isEmailNotifications()) {
            emailService.sendNewFollowerNotification(
                    followed.getEmail(),
                    follower.getUsername());
        }

        notificationService.createNotification(
                followedId,
                followerId,
                NotificationType.NUEVO_SEGUIDOR,
                null
        );
    }

    /**
     * Elimina una relación de seguimiento existente (Dejar de seguir).
     *
     * @param followerId ID del usuario que deja de seguir.
     * @param followedId ID del usuario que estaba siendo seguido.
     * @throws RuntimeException Si la relación no existe.
     */
    @Override
    @Transactional
    public void unfollowUser(Long followerId, Long followedId) {
        Follow follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .orElseThrow(() -> new RuntimeException("No sigues a este usuario"));

        followRepository.delete(follow);
    }

    /**
     * Comprueba si existe una relación de seguimiento entre dos usuarios, independientemente del estado.
     *
     * @param followerId ID del seguidor.
     * @param followedId ID del seguido.
     * @return {@code true} si existe la relación en base de datos, {@code false} en caso contrario.
     */
    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    /**
     * Obtiene la lista de seguidores de un usuario que han sido aceptados.
     *
     * @param userId ID del usuario.
     * @return Lista de relaciones {@link Follow} donde el usuario es el 'followed' y el estado es ACCEPTED.
     */
    @Override
    public List<Follow> getFollowers(Long userId) {
        return followRepository.findByFollowedIdAndStatus(userId, FollowStatus.ACCEPTED);
    }

    /**
     * Obtiene la lista de personas a las que sigue un usuario (aceptadas).
     *
     * @param userId ID del usuario.
     * @return Lista de relaciones {@link Follow} donde el usuario es el 'follower' y el estado es ACCEPTED.
     */
    @Override
    public List<Follow> getFollowing(Long userId) {
        return followRepository.findByFollowerIdAndStatus(userId, FollowStatus.ACCEPTED);
    }
}