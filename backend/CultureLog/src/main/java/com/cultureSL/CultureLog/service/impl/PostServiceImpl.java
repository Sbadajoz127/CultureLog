package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.model.*;
import com.cultureSL.CultureLog.model.enums.NotificationType;
import com.cultureSL.CultureLog.repository.*;
import com.cultureSL.CultureLog.service.FollowService;
import com.cultureSL.CultureLog.service.NotificationService;
import com.cultureSL.CultureLog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio principal para la gestión de Publicaciones (Posts).
 * <p>
 * Orquesta la creación de contenido, la generación del Feed de noticias, y las interacciones
 * sociales (Likes y Comentarios), disparando las notificaciones correspondientes.
 * </p>
 */
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

    /**
     * Crea una nueva publicación y notifica a todos los seguidores del autor.
     *
     * @param userId            ID del autor.
     * @param content           Texto del post.
     * @param linkedMediaItemId (Opcional) ID de un ítem de la biblioteca para adjuntar al post.
     * @return El post creado.
     * @throws RuntimeException Si el usuario o el ítem multimedia no existen.
     */
    @Override
    @Transactional
    public Post createPost(Long userId, String content, Long linkedMediaItemId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(content);

        if (linkedMediaItemId != null) {
            MediaItem item = mediaItemRepository.findById(linkedMediaItemId)
                    .orElseThrow(() -> new RuntimeException("Item multimedia no encontrado"));
            post.setLinkedItem(item);
        }

        Post savedPost = postRepository.save(post);

        List<Follow> followers = followService.getFollowers(userId);

        for (Follow follow : followers) {
            Long recipientId = follow.getFollower().getId();
            
            notificationService.createNotification(
                recipientId,
                userId,
                NotificationType.NUEVO_POST,
                savedPost.getId()
            );
        }

        return savedPost;
    }

    /**
     * Genera el Feed de noticias para un usuario.
     * <p>
     * Recupera posts del propio usuario y de las personas a las que sigue,
     * ordenados cronológicamente.
     * </p>
     *
     * @param userId   ID del usuario que consulta el feed.
     * @param pageable Configuración de paginación.
     * @return Página de posts.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Post> getNewsFeed(Long userId, Pageable pageable) {
        return postRepository.findNewsFeed(userId, pageable);
    }

    /**
     * Recupera los posts creados por un usuario específico (Perfil).
     *
     * @param userId   ID del usuario autor.
     * @param pageable Configuración de paginación.
     * @return Página de posts del usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Post> getPostsByUserId(Long userId, Pageable pageable) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Gestiona la acción de dar o quitar "Me gusta" (Like) a un post.
     * <p>
     * Si el like ya existe, lo elimina (dislike) y decrementa el contador.
     * Si no existe, lo crea y aumenta el contador.
     * Genera una notificación al autor del post (aunque la lógica actual notifica en ambos casos, idealmente solo al dar like).
     * </p>
     *
     * @param postId ID del post.
     * @param userId ID del usuario que interactúa.
     */
    @Override
    @Transactional
    public void toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            postLikeRepository.save(new PostLike(post, user));
            post.setLikeCount(post.getLikeCount() + 1);
        }

        postRepository.save(post);

        notificationService.createNotification(
                post.getAuthor().getId(),
                userId,
                NotificationType.LIKE_POST,
                post.getId()
        );
    }

    /**
     * Añade un comentario a una publicación.
     *
     * @param postId ID del post.
     * @param userId ID del autor del comentario.
     * @param text   Contenido del comentario.
     * @return El comentario guardado.
     */
    @Override
    @Transactional
    public Comment addComment(Long postId, Long userId, String text) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setText(text);

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return commentRepository.save(comment);
    }

    /**
     * Obtiene todos los comentarios asociados a un post, ordenados por antigüedad (ascendente).
     *
     * @param postId ID del post.
     * @return Lista de comentarios.
     */
    @Override
    public List<Comment> getCommentsForPost(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }
}