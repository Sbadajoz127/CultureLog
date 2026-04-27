package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.dto.*;
import com.cultureSL.CultureLog.exception.BadRequestException;
import com.cultureSL.CultureLog.exception.ResourceNotFoundException;
import com.cultureSL.CultureLog.mapper.MediaItemMapper;
import com.cultureSL.CultureLog.mapper.PostMapper;
import com.cultureSL.CultureLog.model.*;
import com.cultureSL.CultureLog.model.enums.FollowStatus;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.Role;
import com.cultureSL.CultureLog.repository.*;
import com.cultureSL.CultureLog.service.AdminService;
import com.cultureSL.CultureLog.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MediaItemRepository mediaItemRepository;
    private final FollowRepository followRepository;
    private final ImageStorageService imageStorageService;
    private final PostMapper postMapper;
    private final MediaItemMapper mediaItemMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listUsers(Pageable pageable, Long userId, String memorySortBy, boolean desc) {
        if (userId != null) {
            Page<User> page = userRepository.findById(userId)
                    .map(user -> (Page<User>) new PageImpl<>(List.of(user), pageable, 1))
                    .orElseGet(() -> new PageImpl<>(List.of(), pageable, 0));
            return page.map(this::toAdminUserResponse);
        }

        if (memorySortBy != null) {
            List<User> allUsers = userRepository.findAll();
            List<AdminUserResponse> dtos = allUsers.stream()
                    .map(this::toAdminUserResponse)
                    .collect(Collectors.toList());

            Comparator<AdminUserResponse> cmp = "postCount".equals(memorySortBy)
                    ? Comparator.comparingLong(AdminUserResponse::getPostCount)
                    : Comparator.comparingLong(AdminUserResponse::getItemCount);
            if (desc) cmp = cmp.reversed();
            dtos.sort(cmp);

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), dtos.size());
            List<AdminUserResponse> slice = start < dtos.size() ? dtos.subList(start, end) : List.of();
            return new PageImpl<>(slice, pageable, dtos.size());
        }

        Page<User> page = userRepository.findAll(pageable);
        return page.map(this::toAdminUserResponse);
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .postCount(postRepository.countByAuthorId(user.getId()))
                .itemCount(mediaItemRepository.countByUserId(user.getId()))
                .followerCount(followRepository.countByFollowedIdAndStatus(user.getId(), FollowStatus.ACCEPTED))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAutocompleteResponse> autocompleteUsers(String q) {
        return userRepository.autocompleteByUsername(q, PageRequest.of(0, 20)).stream()
                .map(user -> UserAutocompleteResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserDetail(Long userId, Long adminUserId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UserSettings settings = target.getSettings();
        long followerCount = followRepository.countByFollowedIdAndStatus(userId, FollowStatus.ACCEPTED);
        long followingCount = followRepository.countByFollowerIdAndStatus(userId, FollowStatus.ACCEPTED);
        long postCnt = postRepository.countByAuthorId(userId);

        List<Post> userPosts = postRepository.findByAuthorIdOrderByCreatedAtDesc(
                userId, PageRequest.of(0, 50)).getContent();
        List<PostResponse> posts = postMapper.toDtoList(userPosts, adminUserId);

        List<MediaItem> items = mediaItemRepository.findByUserIdWithTags(userId);
        List<MediaItemResponse> libraryItems = mediaItemMapper.toDtoList(items);

        return UserProfileResponse.builder()
                .id(target.getId())
                .username(target.getUsername())
                .profilePictureUrl(target.getProfilePictureUrl())
                .profilePrivacy(settings != null ? settings.getProfilePrivacy().name() : "PUBLICO")
                .postCount((int) postCnt)
                .followerCount((int) followerCount)
                .followingCount((int) followingCount)
                .followStatus("NONE")
                .ownProfile(false)
                .showFutureList(true)
                .posts(posts)
                .libraryItems(libraryItems)
                .build();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("No se puede eliminar una cuenta de administrador");
        }

        // Unlink media items referenced by posts from other users
        List<MediaItem> items = mediaItemRepository.findByUserId(userId);
        for (MediaItem item : items) {
            postRepository.unlinkMediaItem(item.getId());
            if (item.getItemImageUrl() != null) {
                imageStorageService.deleteImage(item.getItemImageUrl());
            }
        }

        if (user.getProfilePictureUrl() != null) {
            imageStorageService.deleteImage(user.getProfilePictureUrl());
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPostResponse> listPosts(Pageable pageable, Long authorId, Long linkedItemId) {
        Page<Post> page;
        if (authorId != null || linkedItemId != null) {
            page = postRepository.findAllWithFilters(authorId, linkedItemId, pageable);
        } else {
            page = postRepository.findAllWithAuthorAndItem(pageable);
        }

        return page.map(post ->
                AdminPostResponse.builder()
                        .id(post.getId())
                        .content(post.getContent())
                        .authorUsername(post.getAuthor().getUsername())
                        .authorId(post.getAuthor().getId())
                        .linkedItemTitle(post.getLinkedItem() != null ? post.getLinkedItem().getTitle() : null)
                        .linkedItemId(post.getLinkedItem() != null ? post.getLinkedItem().getId() : null)
                        .createdAt(post.getCreatedAt())
                        .likeCount(post.getLikeCount())
                        .commentCount(post.getCommentCount())
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemAutocompleteResponse> autocompleteLinkedItems(String q) {
        return postRepository.findDistinctLinkedItems(q).stream()
                .map(item -> ItemAutocompleteResponse.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCommentResponse> getPostComments(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));

        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(c -> AdminCommentResponse.builder()
                        .id(c.getId())
                        .text(c.getText())
                        .authorUsername(c.getAuthor().getUsername())
                        .createdAt(c.getCreatedAt())
                        .parentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null)
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));

        Long postId = comment.getPost().getId();
        commentRepository.delete(comment);
        postRepository.updateCommentCount(postId, -1);
    }

    @Override
    @Transactional
    public void deleteMediaItem(Long itemId) {
        MediaItem item = mediaItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));

        postRepository.unlinkMediaItem(itemId);

        if (item.getItemImageUrl() != null) {
            imageStorageService.deleteImage(item.getItemImageUrl());
        }

        mediaItemRepository.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long totalItems = mediaItemRepository.count();
        long totalComments = commentRepository.count();

        Map<String, Long> itemsByMediaType = mediaItemRepository.countGroupedByType().stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Long> itemsByStatus = mediaItemRepository.countGroupedByStatus().stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<AdminStatsResponse.TopUserEntry> topByItems = mediaItemRepository
                .countItemsGroupedByUser(PageRequest.of(0, 5)).stream()
                .map(row -> AdminStatsResponse.TopUserEntry.builder()
                        .username((String) row[0])
                        .count((Long) row[1])
                        .build())
                .toList();

        List<AdminStatsResponse.TopUserEntry> topByPosts = postRepository
                .countPostsGroupedByUser(PageRequest.of(0, 5)).stream()
                .map(row -> AdminStatsResponse.TopUserEntry.builder()
                        .username((String) row[0])
                        .count((Long) row[1])
                        .build())
                .toList();

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalPosts(totalPosts)
                .totalItems(totalItems)
                .totalComments(totalComments)
                .itemsByMediaType(itemsByMediaType)
                .itemsByStatus(itemsByStatus)
                .topUsersByItems(topByItems)
                .topUsersByPosts(topByPosts)
                .build();
    }
}
