package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar las relaciones sociales entre usuarios.
 * <p>
 * Permite realizar acciones de seguimiento (Follow/Unfollow).
 * </p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * Envía una solicitud para seguir a otro usuario.
     * <p>Endpoint: {@code POST /api/users/{userId}/follow?targetId={targetId}}</p>
     *
     * @param userId   ID del usuario que quiere seguir (quien realiza la acción).
     * @param targetId ID del usuario al que se quiere seguir.
     * @return Mensaje de confirmación.
     */
    @PostMapping("/{userId}/follow")
    public ResponseEntity<String> followUser(
            @PathVariable Long userId, 
            @RequestParam Long targetId) {
        
        followService.followUser(userId, targetId);
        return ResponseEntity.ok("Solicitud de seguimiento enviada/aceptada");
    }

    /**
     * Deja de seguir a un usuario previamente seguido.
     * <p>Endpoint: {@code POST /api/users/{userId}/unfollow?targetId={targetId}}</p>
     *
     * @param userId   ID del usuario que deja de seguir.
     * @param targetId ID del usuario que dejará de ser seguido.
     * @return Mensaje de confirmación.
     */
    @PostMapping("/{userId}/unfollow")
    public ResponseEntity<String> unfollowUser(
            @PathVariable Long userId, 
            @RequestParam Long targetId) {
        
        followService.unfollowUser(userId, targetId);
        return ResponseEntity.ok("Dejado de seguir correctamente");
    }
}