package com.culturesl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    
    // Autor
    private Long authorId;
    private String authorName;
    
    // Item Cultural (Peli/Libro/Música)
    private Long linkedItemId;
    private String linkedItemTitle;
    private String linkedItemType;    // "PELICULA", "LIBRO", etc.
    private Integer linkedItemRating; // 1-5

    // Estadísticas
    private int likeCount;
    private int commentCount;
    private boolean likedByCurrentUser;

    // Comentarios (opcional, si decides mostrarlos en la tarjeta)
    // private List<CommentResponse> recentComments;
}