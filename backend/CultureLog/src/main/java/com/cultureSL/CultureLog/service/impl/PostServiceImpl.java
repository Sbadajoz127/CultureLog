package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.exception.ResourceNotFoundException;
import com.cultureSL.CultureLog.exception.UnauthorizedException;
import com.cultureSL.CultureLog.model.*;
import com.cultureSL.CultureLog.model.enums.NotificationType;
import com.cultureSL.CultureLog.repository.*;
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
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final FollowService followService;

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
    public Page<Post> getNewsFeed(Long userId, Pageable pageable) {
        return postRepository.findNewsFeed(userId, pageable);
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
    public void toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            postRepository.updateLikeCount(postId, -1);
        } else {
            try {
                postLikeRepository.save(new PostLike(post, user));
                postRepository.updateLikeCount(postId, 1);
            } catch (DataIntegrityViolationException e) {
                log.debug("Like duplicado ignorado para post {} y usuario {}", postId, userId);
                return;
            }

            if (!post.getAuthor().getId().equals(userId)) {
                notificationService.createNotification(
                        post.getAuthor().getId(),
                        userId,
                        NotificationType.LIKE_POST,
                        post.getId()
                );
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Comment addComment(Long postId, Long userId, String text) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setText(text);

        postRepository.updateCommentCount(postId, 1);

        Comment savedComment = commentRepository.save(comment);

        if (!post.getAuthor().getId().equals(userId)) {
            notificationService.createNotification(
                    post.getAuthor().getId(),
                    userId,
                    NotificationType.COMENTARIO_POST,
                    post.getId()
            );
        }

        return savedComment;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsForPost(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }
}
