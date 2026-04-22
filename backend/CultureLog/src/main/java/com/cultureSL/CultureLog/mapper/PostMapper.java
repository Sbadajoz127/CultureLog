package com.cultureSL.CultureLog.mapper;

import com.cultureSL.CultureLog.dto.CommentResponse;
import com.cultureSL.CultureLog.dto.PostResponse;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.Post;
import com.cultureSL.CultureLog.repository.PostLikeRepository;
import com.cultureSL.CultureLog.repository.PostSaveRepository;
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
    private final PostSaveRepository postSaveRepository;

    /**
     * Convierte un post individual en su DTO de respuesta.
     *
     * @param post          entidad del post a convertir
     * @param currentUserId ID del usuario que visualiza (para determinar si dio like)
     * @return DTO con todos los datos necesarios para renderizar el post
     */
    public PostResponse toDto(Post post, Long currentUserId) {
        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        boolean isSaved = postSaveRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        return buildDto(post, isLiked, isSaved);
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
        Set<Long> savedPostIds = postSaveRepository.findSavedPostIds(currentUserId, postIds);

        return postPage.map(post -> buildDto(post, likedPostIds.contains(post.getId()), savedPostIds.contains(post.getId())));
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
        Set<Long> savedPostIds = postSaveRepository.findSavedPostIds(currentUserId, postIds);

        return posts.stream()
                .map(post -> buildDto(post, likedPostIds.contains(post.getId()), savedPostIds.contains(post.getId())))
                .toList();
    }

    /**
     * Construye el DTO de respuesta a partir de un post y su estado de like/save.
     *
     * @param post    entidad del post
     * @param isLiked indica si el usuario actual dio like a este post
     * @param isSaved indica si el usuario actual guardó este post
     * @return DTO completo del post
     */
    private PostResponse buildDto(Post post, boolean isLiked, boolean isSaved) {
        MediaItem linkedItem = post.getLinkedItem();

        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getUsername())
                .authorProfilePictureUrl(post.getAuthor().getProfilePictureUrl())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .likedByCurrentUser(isLiked)
                .savedByCurrentUser(isSaved)
                .linkedItemId(linkedItem != null ? linkedItem.getId() : null)
                .linkedItemTitle(linkedItem != null ? linkedItem.getTitle() : null)
                .linkedItemType(linkedItem != null ? linkedItem.getType().name() : null)
                .linkedItemRating(linkedItem != null ? linkedItem.getRating() : null)
                .linkedItemImageUrl(linkedItem != null ? linkedItem.getItemImageUrl() : null)
                .linkedItemCreator(linkedItem != null ? linkedItem.getCreator() : null)
                .linkedItemReleaseDate(linkedItem != null ? linkedItem.getReleaseDate() : null)
                .linkedItemGenre(linkedItem != null ? linkedItem.getGenre() : null)
                .linkedItemDescription(linkedItem != null ? linkedItem.getDescription() : null)
                .recentComments(post.getComments().stream()
                        .sorted(Comparator.comparing(c -> c.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(3)
                        .map(c -> CommentResponse.builder()
                                .id(c.getId())
                                .text(c.getText())
                                .authorName(c.getAuthor().getUsername())
                                .authorId(c.getAuthor().getId())
                                .authorProfilePictureUrl(c.getAuthor().getProfilePictureUrl())
                                .parentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null)
                                .createdAt(c.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
