package com.cultureSL.CultureLog.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de salida para mostrar solicitudes de seguimiento pendientes.
 */
@Data
@Builder
public class FollowRequestResponse {
    private Long followerId;
    private String followerUsername;
    private String followerProfilePicture;
    private LocalDateTime createdAt;
}
