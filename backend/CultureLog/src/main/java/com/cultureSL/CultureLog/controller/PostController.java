package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.CommentRequest;
import com.cultureSL.CultureLog.dto.CommentResponse;
import com.cultureSL.CultureLog.dto.LikeResponse;
import com.cultureSL.CultureLog.dto.PostRequest;
import com.cultureSL.CultureLog.dto.PostResponse;
import com.cultureSL.CultureLog.mapper.PostMapper;
import com.cultureSL.CultureLog.model.Post;
import com.cultureSL.CultureLog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de publicaciones del feed social.
 * <p>
 * Expone endpoints para crear posts, consultar el feed de noticias personalizado,
 * dar/quitar likes y añadir comentarios. Requiere autenticación JWT.
 * </p>
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    /**
     * Crea una nueva publicación en el feed del usuario.
     * <p>Endpoint: {@code POST /api/posts}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param request        datos del post (contenido y opcionalmente ítem vinculado)
     * @return HTTP 200 con el post creado en formato {@link PostResponse}
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            Authentication authentication,
            @Valid @RequestBody PostRequest request) {

        Long userId = (Long) authentication.getPrincipal();
        Post createdPost = postService.createPost(userId, request.getContent(), request.getLinkedMediaItemId());
        return ResponseEntity.status(HttpStatus.CREATED).body(postMapper.toDto(createdPost, userId));
    }

    /**
     * Obtiene el feed de noticias personalizado del usuario autenticado.
     * <p>Endpoint: {@code GET /api/posts/feed?page=0&size=10}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param page           número de página (por defecto 0)
     * @param size           tamaño de página (por defecto 10)
     * @return HTTP 200 con la página de posts en formato {@link PostResponse}
     */
    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> getFeed(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = (Long) authentication.getPrincipal();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return ResponseEntity.ok(postService.getNewsFeed(userId, pageable));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(postService.getPostById(postId, userId));
    }

    /**
     * Alterna el "Me gusta" en una publicación (like/unlike).
     * <p>Endpoint: {@code POST /api/posts/{postId}/like}</p>
     *
     * @param postId         ID del post
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 con el conteo real de likes y estado actual
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeResponse> toggleLike(@PathVariable Long postId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        LikeResponse response = postService.toggleLike(postId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Añade un comentario a una publicación.
     * <p>Endpoint: {@code POST /api/posts/{postId}/comments}</p>
     *
     * @param postId         ID del post a comentar
     * @param authentication contexto de autenticación con el ID del usuario
     * @param request        datos del comentario (texto)
     * @return HTTP 200 sin contenido
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            Authentication authentication,
            @Valid @RequestBody CommentRequest request) {

        Long userId = (Long) authentication.getPrincipal();
        var saved = postService.addComment(postId, userId, request.getText(), request.getParentCommentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.builder()
                .id(saved.getId())
                .text(saved.getText())
                .authorName(saved.getAuthor().getUsername())
                .authorId(saved.getAuthor().getId())
                .authorProfilePictureUrl(saved.getAuthor().getProfilePictureUrl())
                .parentCommentId(saved.getParentComment() != null ? saved.getParentComment().getId() : null)
                .createdAt(saved.getCreatedAt())
                .build());
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(postService.getCommentResponsesForPost(postId, userId));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Alterna el guardado de una publicación.
     * <p>Endpoint: {@code POST /api/posts/{postId}/save}</p>
     *
     * @param postId         ID del post
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 con el estado de guardado (true = guardado, false = quitado)
     */
    @PostMapping("/{postId}/save")
    public ResponseEntity<java.util.Map<String, Boolean>> toggleSave(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean saved = postService.toggleSave(postId, userId);
        return ResponseEntity.ok(java.util.Map.of("saved", saved));
    }

    /**
     * Obtiene los posts guardados por el usuario autenticado.
     * <p>Endpoint: {@code GET /api/posts/saved?page=0&size=10}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param page           número de página (por defecto 0)
     * @param size           tamaño de página (por defecto 10)
     * @return HTTP 200 con la página de posts guardados
     */
    @GetMapping("/saved")
    public ResponseEntity<Page<PostResponse>> getSavedPosts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        return ResponseEntity.ok(postService.getSavedPosts(userId, pageable));
    }

    /**
     * Obtiene los posts que el usuario autenticado ha dado like.
     * <p>Endpoint: {@code GET /api/posts/liked?page=0&size=10}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param page           número de página (por defecto 0)
     * @param size           tamaño de página (por defecto 10)
     * @return HTTP 200 con la página de posts con like
     */
    @GetMapping("/liked")
    public ResponseEntity<Page<PostResponse>> getLikedPosts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        return ResponseEntity.ok(postService.getLikedPosts(userId, pageable));
    }
}
