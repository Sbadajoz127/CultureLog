package com.cultureSL.CultureLog.mapper;

import com.cultureSL.CultureLog.dto.CommentResponse;
import com.cultureSL.CultureLog.dto.PostResponse;
import com.cultureSL.CultureLog.model.Post;
import com.cultureSL.CultureLog.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Componente encargado de transformar entidades {@link Post} en objetos de transferencia {@link PostResponse}.
 * <p>
 * Este mapper no solo copia datos, sino que enriquece la respuesta con lógica de negocio específica para la vista,
 * como verificar si el usuario actual ha dado "like" al post o limitar la vista previa de comentarios.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class PostMapper {

    private final PostLikeRepository postLikeRepository;

    /**
     * Convierte una entidad Post a su DTO correspondiente.
     * <p>
     * Realiza las siguientes operaciones de enriquecimiento:
     * <ul>
     * <li>Consulta si el {@code currentUserId} ha dado like al post.</li>
     * <li>Aplana los datos del {@code MediaItem} vinculado (si existe) para facilitar su renderizado.</li>
     * <li>Transforma y limita la lista de comentarios a los 3 más recientes.</li>
     * </ul>
     *
     * @param post          La entidad Post recuperada de la base de datos.
     * @param currentUserId El ID del usuario que está solicitando la información (para calcular {@code likedByCurrentUser}).
     * @return Un objeto {@link PostResponse} listo para ser enviado al cliente.
     */
    public PostResponse toDto(Post post, Long currentUserId) {
        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);

        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getUsername())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .likedByCurrentUser(isLiked)
                .linkedItemId(post.getLinkedItem() != null ? post.getLinkedItem().getId() : null)
                .linkedItemTitle(post.getLinkedItem() != null ? post.getLinkedItem().getTitle() : null)
                .linkedItemType(post.getLinkedItem() != null ? post.getLinkedItem().getType().name() : null)
                .linkedItemRating(post.getLinkedItem() != null ? post.getLinkedItem().getRating() : null)
                .recentComments(post.getComments().stream()
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