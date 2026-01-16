package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.Follow;
import com.cultureSL.CultureLog.model.enums.FollowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    List<Follow> findByFollowerIdAndStatus(Long followerId, FollowStatus status);

    List<Follow> findByFollowedIdAndStatus(Long followedId, FollowStatus status);
}