package com.cultureSL.CultureLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalPosts;
    private long totalItems;
    private long totalComments;

    /** Distribución de items por tipo de medio (ej. PELICULA -> 42). */
    private Map<String, Long> itemsByMediaType;

    /** Distribución de items por estado (ej. VISTO -> 105). */
    private Map<String, Long> itemsByStatus;

    /** Usuarios con más items en su biblioteca. */
    private List<TopUserEntry> topUsersByItems;

    /** Usuarios con más posts publicados. */
    private List<TopUserEntry> topUsersByPosts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopUserEntry {
        private String username;
        private long count;
    }
}
