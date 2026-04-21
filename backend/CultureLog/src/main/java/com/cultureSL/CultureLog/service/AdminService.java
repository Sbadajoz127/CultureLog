package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Servicio de administración con operaciones privilegiadas que no verifican ownership.
 */
public interface AdminService {

    Page<AdminUserResponse> listUsers(Pageable pageable, String search);

    UserProfileResponse getUserDetail(Long userId, Long adminUserId);

    void deleteUser(Long userId);

    Page<AdminPostResponse> listPosts(Pageable pageable);

    List<AdminCommentResponse> getPostComments(Long postId);

    void deletePost(Long postId);

    void deleteComment(Long commentId);

    void deleteMediaItem(Long itemId);

    AdminStatsResponse getStats();
}
