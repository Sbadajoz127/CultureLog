package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.model.Follow;
import java.util.List;

public interface FollowService {
    
    void followUser(Long followerId, Long followedId);

    void unfollowUser(Long followerId, Long followedId);

    boolean isFollowing(Long followerId, Long followedId);
    
    List<Follow> getFollowers(Long userId);
    
    List<Follow> getFollowing(Long userId);
}