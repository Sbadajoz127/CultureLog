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
public class AdminCommentResponse {
    private Long id;
    private String text;
    private String authorUsername;
    private LocalDateTime createdAt;
    private Long parentCommentId;
}
