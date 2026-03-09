package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.CommentRequest;
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
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postService.getNewsFeed(userId, pageable);

        Page<PostResponse> dtoPage = postMapper.toPageDto(postPage, userId);

        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Alterna el "Me gusta" en una publicación (like/unlike).
     * <p>Endpoint: {@code POST /api/posts/{postId}/like}</p>
     *
     * @param postId         ID del post
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 sin contenido
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable Long postId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.toggleLike(postId, userId);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> addComment(
            @PathVariable Long postId,
            Authentication authentication,
            @Valid @RequestBody CommentRequest request) {

        Long userId = (Long) authentication.getPrincipal();
        postService.addComment(postId, userId, request.getText());
        return ResponseEntity.ok().build();
    }
}
