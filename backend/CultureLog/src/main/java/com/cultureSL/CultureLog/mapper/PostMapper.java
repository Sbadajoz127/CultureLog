package com.cultureSL.CultureLog.mapper;

import com.cultureSL.CultureLog.dto.CommentResponse;
import com.cultureSL.CultureLog.dto.PostResponse;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.Post;
import com.cultureSL.CultureLog.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Page;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Componente encargado de convertir entidades {@link Post} en DTOs {@link PostResponse}.
 * <p>
 * Enriquece cada post con información de likes del usuario actual, datos del autor,
 * resumen del ítem multimedia vinculado y los comentarios más recientes.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class PostMapper {

    private final PostLikeRepository postLikeRepository;

    /**
     * Convierte un post individual en su DTO de respuesta.
     *
     * @param post          entidad del post a convertir
     * @param currentUserId ID del usuario que visualiza (para determinar si dio like)
     * @return DTO con todos los datos necesarios para renderizar el post
     */
    public PostResponse toDto(Post post, Long currentUserId) {
        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        return buildDto(post, isLiked);
    }

    /**
     * Convierte una página de posts en DTOs de respuesta de forma optimizada.
     * <p>
     * Resuelve en bloque qué posts tienen like del usuario actual mediante
     * una sola consulta, evitando el problema N+1.
     * </p>
     *
     * @param postPage      página de entidades de post
     * @param currentUserId ID del usuario que visualiza
     * @return página de DTOs listos para el feed
     */
    public Page<PostResponse> toPageDto(Page<Post> postPage, Long currentUserId) {
        List<Post> posts = postPage.getContent();
        if (posts.isEmpty()) {
            return Page.empty(postPage.getPageable());
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Set<Long> likedPostIds = postLikeRepository.findLikedPostIds(currentUserId, postIds);

        return postPage.map(post -> buildDto(post, likedPostIds.contains(post.getId())));
    }

    /**
     * Convierte una lista de posts en DTOs de respuesta de forma optimizada.
     * <p>
     * Resuelve en bloque qué posts tienen like del usuario actual mediante
     * una sola consulta, en lugar de consultar uno a uno.
     * </p>
     *
     * @param posts         lista de entidades de post
     * @param currentUserId ID del usuario que visualiza
     * @return lista de DTOs listos para el feed
     */
    public List<PostResponse> toDtoList(List<Post> posts, Long currentUserId) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Set<Long> likedPostIds = postLikeRepository.findLikedPostIds(currentUserId, postIds);

        return posts.stream()
                .map(post -> buildDto(post, likedPostIds.contains(post.getId())))
                .toList();
    }

    /**
     * Construye el DTO de respuesta a partir de un post y su estado de like.
     *
     * @param post    entidad del post
     * @param isLiked indica si el usuario actual dio like a este post
     * @return DTO completo del post
     */
    private PostResponse buildDto(Post post, boolean isLiked) {
        MediaItem linkedItem = post.getLinkedItem();

        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getUsername())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .likedByCurrentUser(isLiked)
                .linkedItemId(linkedItem != null ? linkedItem.getId() : null)
                .linkedItemTitle(linkedItem != null ? linkedItem.getTitle() : null)
                .linkedItemType(linkedItem != null ? linkedItem.getType().name() : null)
                .linkedItemRating(linkedItem != null ? linkedItem.getRating() : null)
                .recentComments(post.getComments().stream()
                        .sorted(Comparator.comparing(c -> c.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(3)
                        .map(c -> new CommentResponse(
                                c.getId(),
                                c.getText(),
                                c.getAuthor().getUsername(),
                                c.getAuthor().getId(),
                                c.getCreatedAt()))
                        .collect(Collectors.toList()))
                .build();
    }
}
