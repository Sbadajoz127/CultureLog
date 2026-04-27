package com.cultureSL.CultureLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPostResponse {
    private Long id;
    private String content;
    private String authorUsername;
    private Long authorId;
    private String linkedItemTitle;
    private Long linkedItemId;
    private LocalDateTime createdAt;
    private int likeCount;
    private int commentCount;
}
