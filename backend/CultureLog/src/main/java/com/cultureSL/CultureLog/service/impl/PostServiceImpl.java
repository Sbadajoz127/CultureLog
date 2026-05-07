package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.dto.LikeResponse;
import com.cultureSL.CultureLog.dto.CommentResponse;
import com.cultureSL.CultureLog.dto.PostResponse;
import com.cultureSL.CultureLog.exception.BadRequestException;
import com.cultureSL.CultureLog.exception.ResourceNotFoundException;
import com.cultureSL.CultureLog.exception.UnauthorizedException;
import com.cultureSL.CultureLog.mapper.PostMapper;
import com.cultureSL.CultureLog.model.*;
import com.cultureSL.CultureLog.model.enums.NotificationType;
import com.cultureSL.CultureLog.model.enums.ProfilePrivacy;
import com.cultureSL.CultureLog.model.enums.Role;
import com.cultureSL.CultureLog.repository.*;
import com.cultureSL.CultureLog.service.EmailService;
import com.cultureSL.CultureLog.service.FollowService;
import com.cultureSL.CultureLog.service.NotificationService;
import com.cultureSL.CultureLog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de publicaciones del feed social.
 * <p>
 * Gestiona la creación de posts (con notificación a seguidores), la generación
 * del feed personalizado, y las interacciones sociales (likes y comentarios)
 * con actualización de contadores desnormalizados.
 * </p>
 *
 * @see PostService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaItemRepository mediaItemRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostSaveRepository postSaveRepository;
    private final CommentRepository commentRepository;
    private final PostMapper postMapper;
    private final NotificationService notificationService;
    private final FollowService followService;
    private final EmailService emailService;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Post createPost(Long userId, String content, Long linkedMediaItemId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(content);

        if (linkedMediaItemId != null) {
            MediaItem item = mediaItemRepository.findById(linkedMediaItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Item multimedia no encontrado"));
            if (!item.getUser().getId().equals(userId)) {
                throw new UnauthorizedException("No puedes vincular un item que no te pertenece");
            }
            post.setLinkedItem(item);
            post.setLinkedItemTitle(item.getTitle());
            post.setLinkedItemType(item.getType());
            post.setLinkedItemRating(item.getRating());
            post.setLinkedItemImageUrl(item.getItemImageUrl());
            post.setLinkedItemCreator(item.getCreator());
            post.setLinkedItemReleaseDate(item.getReleaseDate());
            post.setLinkedItemGenre(item.getGenre());
            post.setLinkedItemDescription(item.getDescription());
            post.setLinkedItemAlbum(item.getAlbum());
        }

        Post savedPost = postRepository.save(post);

        List<Long> followerIds = followService.getFollowerIds(userId);
        notificationService.createBulkNotificationsAsync(
                followerIds, userId, NotificationType.NUEVO_POST, savedPost.getId());

        return savedPost;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getNewsFeed(Long userId, Pageable pageable) {
        Page<Post> postPage = postRepository.findNewsFeed(userId, pageable);
        return postMapper.toPageDto(postPage, userId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
        validatePostAccess(post, currentUserId);
        return postMapper.toDto(post, currentUserId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<Post> getPostsByUserId(Long userId, Pageable pageable) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public LikeResponse toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
        boolean liked;
        int delta;

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            postLikeRepository.flush();
            liked = false;
            delta = -1;
        } else {
            try {
                postLikeRepository.save(new PostLike(post, user));
                liked = true;
                delta = 1;
            } catch (DataIntegrityViolationException e) {
                log.debug("Like duplicado ignorado para post {} y usuario {}", postId, userId);
                return new LikeResponse(post.getLikeCount(), true);
            }

            if (!post.getAuthor().getId().equals(userId)) {
                notificationService.createNotification(
                        post.getAuthor().getId(),
                        userId,
                        NotificationType.LIKE_POST,
                        post.getId()
                );
                UserSettings authorSettings = post.getAuthor().getSettings();
                if (authorSettings != null && authorSettings.isEmailNotifications()) {
                    emailService.sendLikeNotification(post.getAuthor().getEmail(), user.getUsername());
                }
            }
        }

        postRepository.updateLikeCount(postId, delta);
        int newCount = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"))
                .getLikeCount();

        return new LikeResponse(newCount, liked);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Comment addComment(Long postId, Long userId, String text, Long parentCommentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UserSettings postAuthorSettings = post.getAuthor().getSettings();
        if (postAuthorSettings != null && !postAuthorSettings.isAllowComments()) {
            throw new BadRequestException("El autor ha deshabilitado los comentarios en sus publicaciones");
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setText(text);

        if (parentCommentId != null) {
            Comment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comentario padre no encontrado"));
            if (!parent.getPost().getId().equals(postId)) {
                throw new BadRequestException("El comentario padre no pertenece a esta publicación");
            }
            comment.setParentComment(parent);
        }

        postRepository.updateCommentCount(postId, 1);

        Comment savedComment = commentRepository.save(comment);

        if (!post.getAuthor().getId().equals(userId)) {
            notificationService.createNotification(
                    post.getAuthor().getId(),
                    userId,
                    NotificationType.COMENTARIO_POST,
                    post.getId()
            );
            UserSettings authorSettings = post.getAuthor().getSettings();
            if (authorSettings != null && authorSettings.isEmailNotifications()) {
                emailService.sendCommentNotification(post.getAuthor().getEmail(), author.getUsername());
            }
        }

        return savedComment;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));

        Long postAuthorId = comment.getPost().getAuthor().getId();
        Long commentAuthorId = comment.getAuthor().getId();
        if (!currentUserId.equals(commentAuthorId) && !currentUserId.equals(postAuthorId)) {
            throw new UnauthorizedException("No tienes permiso para eliminar este comentario");
        }

        Long postId = comment.getPost().getId();
        Post post = comment.getPost();
        
        // Limpiar referencia del padre si es una respuesta
        if (comment.getParentComment() != null) {
            comment.getParentComment().getReplies().remove(comment);
        }
        
        // Eliminar de la colección del post para sincronizar la relación bidireccional
        post.getComments().remove(comment);
        
        commentRepository.delete(comment);
        commentRepository.flush();
        postRepository.updateCommentCount(postId, -1);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));

        if (!currentUserId.equals(post.getAuthor().getId())) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta publicación");
        }

        postRepository.delete(post);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsForPost(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentResponsesForPost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
        validatePostAccess(post, currentUserId);

        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId())
                        .text(c.getText())
                        .authorId(c.getAuthor().getId())
                        .authorName(c.getAuthor().getUsername())
                        .authorProfilePictureUrl(c.getAuthor().getProfilePictureUrl())
                        .parentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null)
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
    }

    private void validatePostAccess(Post post, Long currentUserId) {
        Long authorId = post.getAuthor().getId();
        if (authorId.equals(currentUserId)) return;

        User viewer = userRepository.findById(currentUserId).orElse(null);
        if (viewer != null && viewer.getRole() == Role.ADMIN) return;

        UserSettings settings = post.getAuthor().getSettings();
        ProfilePrivacy privacy = settings != null ? settings.getProfilePrivacy() : ProfilePrivacy.PUBLICO;
        boolean canAccess = privacy == ProfilePrivacy.PUBLICO || followService.isFollowing(currentUserId, authorId);

        if (!canAccess) {
            throw new UnauthorizedException("No tienes permiso para ver esta publicación");
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public boolean toggleSave(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        var existingSave = postSaveRepository.findByPostIdAndUserId(postId, userId);
        if (existingSave.isPresent()) {
            postSaveRepository.delete(existingSave.get());
            return false;
        } else {
            postSaveRepository.save(new PostSave(post, user));
            return true;
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getSavedPosts(Long userId, Pageable pageable) {
        Page<Post> savedPosts = postRepository.findSavedPostsByUserId(userId, pageable);
        return postMapper.toPageDto(savedPosts, userId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getLikedPosts(Long userId, Pageable pageable) {
        Page<Post> likedPosts = postRepository.findLikedPostsByUserId(userId, pageable);
        return postMapper.toPageDto(likedPosts, userId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String query, Long userId, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Post> posts = postRepository.searchPosts(query.trim(), userId, pageable);
        return postMapper.toPageDto(posts, userId);
    }
}
