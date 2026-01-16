package com.cultureSL.CultureLog.dto;

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
    
    private Long authorId;
    private String authorName;
    
    private Long linkedItemId;
    private String linkedItemTitle;
    private String linkedItemType;
    private Integer linkedItemRating;

    private int likeCount;
    private int commentCount;
    private boolean likedByCurrentUser;

    private List<CommentResponse> recentComments;
}