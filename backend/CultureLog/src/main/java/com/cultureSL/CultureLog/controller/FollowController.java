package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para las operaciones de seguimiento entre usuarios.
 * <p>
 * Permite seguir y dejar de seguir a otros usuarios. Requiere autenticación JWT.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * Envía una solicitud de seguimiento a otro usuario.
     * <p>Endpoint: {@code POST /api/users/follow?targetId=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario actual
     * @param targetId       ID del usuario a seguir
     * @return HTTP 200 con mensaje de confirmación
     */
    @PostMapping("/follow")
    public ResponseEntity<String> followUser(
            Authentication authentication,
            @RequestParam Long targetId) {

        Long userId = (Long) authentication.getPrincipal();
        followService.followUser(userId, targetId);
        return ResponseEntity.ok("Solicitud de seguimiento enviada/aceptada");
    }

    /**
     * Deja de seguir a un usuario.
     * <p>Endpoint: {@code DELETE /api/users/follow?targetId=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario actual
     * @param targetId       ID del usuario a dejar de seguir
     * @return HTTP 200 con mensaje de confirmación
     */
    @DeleteMapping("/follow")
    public ResponseEntity<String> unfollowUser(
            Authentication authentication,
            @RequestParam Long targetId) {

        Long userId = (Long) authentication.getPrincipal();
        followService.unfollowUser(userId, targetId);
        return ResponseEntity.ok("Dejado de seguir correctamente");
    }
}
