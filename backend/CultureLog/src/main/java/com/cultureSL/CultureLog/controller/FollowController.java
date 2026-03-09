package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para las operaciones de seguimiento entre usuarios.
 * <p>
 * Permite seguir, dejar de seguir y gestionar solicitudes pendientes
 * (aceptar/rechazar) entre usuarios. Requiere autenticación JWT.
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

    /**
     * Acepta una solicitud de seguimiento pendiente.
     * <p>Endpoint: {@code POST /api/users/follow/accept?followerId=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario que acepta
     * @param followerId     ID del usuario que envió la solicitud
     * @return HTTP 200 con mensaje de confirmación
     */
    @PostMapping("/follow/accept")
    public ResponseEntity<String> acceptFollow(
            Authentication authentication,
            @RequestParam Long followerId) {

        Long userId = (Long) authentication.getPrincipal();
        followService.acceptFollowRequest(userId, followerId);
        return ResponseEntity.ok("Solicitud de seguimiento aceptada");
    }

    /**
     * Rechaza una solicitud de seguimiento pendiente.
     * <p>Endpoint: {@code POST /api/users/follow/reject?followerId=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario que rechaza
     * @param followerId     ID del usuario que envió la solicitud
     * @return HTTP 200 con mensaje de confirmación
     */
    @PostMapping("/follow/reject")
    public ResponseEntity<String> rejectFollow(
            Authentication authentication,
            @RequestParam Long followerId) {

        Long userId = (Long) authentication.getPrincipal();
        followService.rejectFollowRequest(userId, followerId);
        return ResponseEntity.ok("Solicitud de seguimiento rechazada");
    }
}
