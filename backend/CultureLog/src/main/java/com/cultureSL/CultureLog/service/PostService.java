package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.dto.LikeResponse;
import com.cultureSL.CultureLog.dto.CommentResponse;
import com.cultureSL.CultureLog.dto.PostResponse;
import com.cultureSL.CultureLog.model.Comment;
import com.cultureSL.CultureLog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Servicio que gestiona las publicaciones del feed social.
 * <p>
 * Define el contrato para la creación de posts, generación del feed de noticias,
 * interacciones sociales (likes) y comentarios.
 * </p>
 */
public interface PostService {

    /**
     * Crea una nueva publicación en el feed.
     *
     * @param userId            ID del autor
     * @param content           contenido textual del post
     * @param linkedMediaItemId ID del ítem multimedia vinculado (puede ser {@code null})
     * @return el post creado con su ID generado
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si el usuario o el ítem vinculado no existen
     */
    Post createPost(Long userId, String content, Long linkedMediaItemId);

    /**
     * Genera el feed de noticias personalizado para un usuario.
     * <p>
     * Incluye publicaciones propias y de usuarios seguidos con estado ACCEPTED,
     * ordenadas de más reciente a más antigua. Devuelve DTOs ya mapeados
     * para garantizar que las colecciones lazy se resuelven dentro de la transacción.
     * </p>
     *
     * @param userId   ID del usuario que visualiza el feed
     * @param pageable configuración de paginación
     * @return página de DTOs del feed
     */
    Page<PostResponse> getNewsFeed(Long userId, Pageable pageable);

    /**
     * Obtiene el detalle de una publicación concreta.
     *
     * @param postId         ID de la publicación
     * @param currentUserId  ID del usuario que intenta verla
     * @return DTO de la publicación
     */
    PostResponse getPostById(Long postId, Long currentUserId);

    /**
     * Obtiene las publicaciones de un usuario específico (vista de perfil).
     *
     * @param userId   ID del usuario autor
     * @param pageable configuración de paginación
     * @return página de posts del usuario ordenados cronológicamente
     */
    Page<Post> getPostsByUserId(Long userId, Pageable pageable);

    /**
     * Alterna el estado de "Me gusta" en una publicación.
     * <p>
     * Si el usuario ya dio like, lo elimina; si no, lo crea.
     * Actualiza el contador desnormalizado del post y genera una notificación al autor.
     * </p>
     *
     * @param postId ID de la publicación
     * @param userId ID del usuario que interactúa
     * @return respuesta con el conteo real de likes y si el usuario actual tiene like
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si el post no existe
     */
    LikeResponse toggleLike(Long postId, Long userId);

    /**
     * Añade un comentario a una publicación.
     *
     * @param postId ID de la publicación
     * @param userId ID del usuario que comenta
     * @param text   contenido textual del comentario
     * @return el comentario creado
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si el post o el usuario no existen
     */
    Comment addComment(Long postId, Long userId, String text, Long parentCommentId);

    /**
     * Elimina un comentario.
     * Puede eliminarlo el autor del comentario o el autor de la publicación.
     *
     * @param commentId      ID del comentario
     * @param currentUserId  ID del usuario autenticado
     */
    void deleteComment(Long commentId, Long currentUserId);

    /**
     * Elimina una publicación.
     * Solo el autor de la publicación puede eliminarla.
     *
     * @param postId         ID de la publicación
     * @param currentUserId  ID del usuario autenticado
     */
    void deletePost(Long postId, Long currentUserId);

    /**
     * Recupera todos los comentarios de una publicación ordenados cronológicamente.
     *
     * @param postId ID de la publicación
     * @return lista de comentarios ordenados del más antiguo al más reciente
     */
    List<Comment> getCommentsForPost(Long postId);

    /**
     * Recupera todos los comentarios de una publicación en formato DTO.
     *
     * @param postId ID de la publicación
     * @return lista de comentarios
     */
    List<CommentResponse> getCommentResponsesForPost(Long postId, Long currentUserId);

    /**
     * Alterna el estado de guardado de una publicación.
     *
     * @param postId ID de la publicación
     * @param userId ID del usuario
     * @return true si la publicación quedó guardada, false si se eliminó el guardado
     */
    boolean toggleSave(Long postId, Long userId);

    /**
     * Obtiene los posts guardados por un usuario.
     *
     * @param userId   ID del usuario
     * @param pageable configuración de paginación
     * @return página de posts guardados
     */
    Page<PostResponse> getSavedPosts(Long userId, Pageable pageable);

    /**
     * Obtiene los posts que el usuario ha dado like.
     *
     * @param userId   ID del usuario
     * @param pageable configuración de paginación
     * @return página de posts con like
     */
    Page<PostResponse> getLikedPosts(Long userId, Pageable pageable);
}
