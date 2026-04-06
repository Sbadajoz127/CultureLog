package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.dto.FollowRequestResponse;
import com.cultureSL.CultureLog.exception.BadRequestException;
import com.cultureSL.CultureLog.exception.ResourceNotFoundException;
import com.cultureSL.CultureLog.model.Follow;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.model.UserSettings;
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
 * Implementación del servicio de gestión de relaciones de seguimiento.
 * <p>
 * Gestiona la lógica de seguir/dejar de seguir usuarios, teniendo en cuenta
 * la privacidad del perfil destino para determinar si la relación se acepta
 * automáticamente o queda pendiente de aprobación. Además, genera notificaciones
 * internas y emails al usuario seguido.
 * </p>
 *
 * @see FollowService
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void followUser(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new BadRequestException("No puedes seguirte a ti mismo");
        }

        followRepository.findByFollowerIdAndFollowedId(followerId, followedId).ifPresent(existing -> {
            if (existing.getStatus() == FollowStatus.ACCEPTED) {
                throw new BadRequestException("Ya sigues a este usuario");
            } else if (existing.getStatus() == FollowStatus.PENDING) {
                throw new BadRequestException("Ya tienes una solicitud de seguimiento pendiente");
            } else if (existing.getStatus() == FollowStatus.BLOCKED) {
                throw new BadRequestException("No puedes seguir a este usuario");
            }
        });

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario seguidor no encontrado"));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario a seguir no encontrado"));

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);

        UserSettings settings = followed.getSettings();
        if (settings != null && settings.getProfilePrivacy() == ProfilePrivacy.PRIVADO) {
            follow.setStatus(FollowStatus.PENDING);
        } else {
            follow.setStatus(FollowStatus.ACCEPTED);
        }

        followRepository.save(follow);

        if (settings != null && settings.isEmailNotifications()) {
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

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void unfollowUser(Long followerId, Long followedId) {
        Follow follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .orElseThrow(() -> new ResourceNotFoundException("No sigues a este usuario"));

        followRepository.delete(follow);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.existsByFollowerIdAndFollowedIdAndStatus(followerId, followedId, FollowStatus.ACCEPTED);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Follow> getFollowers(Long userId) {
        return followRepository.findByFollowedIdAndStatus(userId, FollowStatus.ACCEPTED);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Long> getFollowerIds(Long userId) {
        return followRepository.findFollowerIdsByFollowedIdAndStatus(userId, FollowStatus.ACCEPTED);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Follow> getFollowing(Long userId) {
        return followRepository.findByFollowerIdAndStatus(userId, FollowStatus.ACCEPTED);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void acceptFollowRequest(Long followedId, Long followerId) {
        Follow follow = followRepository.findByFollowerIdAndFollowedIdAndStatus(followerId, followedId, FollowStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de seguimiento no encontrada"));

        follow.setStatus(FollowStatus.ACCEPTED);
        followRepository.save(follow);

        notificationService.createNotification(
                followerId,
                followedId,
                NotificationType.NUEVO_SEGUIDOR,
                null
        );
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void rejectFollowRequest(Long followedId, Long followerId) {
        Follow follow = followRepository.findByFollowerIdAndFollowedIdAndStatus(followerId, followedId, FollowStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de seguimiento no encontrada"));

        followRepository.delete(follow);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<FollowRequestResponse> getPendingRequests(Long userId) {
        return followRepository.findByFollowedIdAndStatus(userId, FollowStatus.PENDING)
                .stream()
                .map(f -> FollowRequestResponse.builder()
                        .followerId(f.getFollower().getId())
                        .followerUsername(f.getFollower().getUsername())
                        .followerProfilePicture(f.getFollower().getProfilePictureUrl())
                        .createdAt(f.getCreatedAt())
                        .build())
                .toList();
    }
}
