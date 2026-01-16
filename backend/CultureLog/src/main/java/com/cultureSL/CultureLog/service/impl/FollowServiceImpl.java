package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.model.Follow;
import com.cultureSL.CultureLog.model.ProfilePrivacy;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.model.enums.FollowStatus;
import com.cultureSL.CultureLog.model.enums.NotificationType;
import com.cultureSL.CultureLog.repository.FollowRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.EmailService;
import com.cultureSL.CultureLog.service.FollowService;
import com.cultureSL.CultureLog.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

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

    @Override
    @Transactional
    public void unfollowUser(Long followerId, Long followedId) {
        Follow follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .orElseThrow(() -> new RuntimeException("No sigues a este usuario"));

        followRepository.delete(follow);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Override
    public List<Follow> getFollowers(Long userId) {
        return followRepository.findByFollowedIdAndStatus(userId, FollowStatus.ACCEPTED);
    }

    @Override
    public List<Follow> getFollowing(Long userId) {
        return followRepository.findByFollowerIdAndStatus(userId, FollowStatus.ACCEPTED);
    }
}