package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.CommentRequest;
import com.cultureSL.CultureLog.dto.PostRequest;
import com.cultureSL.CultureLog.dto.PostResponse;
import com.cultureSL.CultureLog.mapper.PostMapper;
import com.cultureSL.CultureLog.model.Post;
import com.cultureSL.CultureLog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de publicaciones y el feed social.
 * <p>
 * Maneja la creación de posts, la generación del timeline y las interacciones (likes/comentarios).
 * </p>
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    /**
     * Crea una nueva publicación.
     * <p>Endpoint: {@code POST /api/posts}</p>
     *
     * @param userId  ID del autor.
     * @param request Datos del post (contenido y posible item vinculado).
     * @return El post creado convertido a DTO.
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestParam Long userId,
            @RequestBody PostRequest request) {
        
        Post createdPost = postService.createPost(userId, request.getContent(), request.getLinkedMediaItemId());
        return ResponseEntity.ok(postMapper.toDto(createdPost, userId));
    }

    /**
     * Obtiene el Feed de noticias para un usuario (timeline).
     * <p>Endpoint: {@code GET /api/posts/feed}</p>
     *
     * @param userId ID del usuario que consulta el feed.
     * @param page   Número de página (por defecto 0).
     * @param size   Tamaño de página (por defecto 10).
     * @return Página de posts (DTOs).
     */
    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> getFeed(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postService.getNewsFeed(userId, pageable);

        Page<PostResponse> dtoPage = postPage.map(post -> postMapper.toDto(post, userId));

        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Alterna el estado de "Me gusta" en un post (Like/Dislike).
     * <p>Endpoint: {@code POST /api/posts/{postId}/like}</p>
     *
     * @param postId ID del post.
     * @param userId ID del usuario que da el like.
     * @return {@code 200 OK}.
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable Long postId, @RequestParam Long userId) {
        postService.toggleLike(postId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Añade un comentario a un post.
     * <p>Endpoint: {@code POST /api/posts/{postId}/comments}</p>
     *
     * @param postId  ID del post.
     * @param userId  ID del autor del comentario.
     * @param request Contenido del comentario.
     * @return {@code 200 OK}.
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Void> addComment(
            @PathVariable Long postId,
            @RequestParam Long userId,
            @RequestBody CommentRequest request) {
        
        postService.addComment(postId, userId, request.getText());
        return ResponseEntity.ok().build();
    }
}