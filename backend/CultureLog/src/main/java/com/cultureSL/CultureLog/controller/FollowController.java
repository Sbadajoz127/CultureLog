package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.FollowRequestResponse;
import com.cultureSL.CultureLog.dto.UserSuggestionResponse;
import com.cultureSL.CultureLog.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para las operaciones de seguimiento entre usuarios.
 * <p>
 * Permite seguir, dejar de seguir y gestionar solicitudes pendientes
 * (aceptar/rechazar) entre usuarios. Requiere autenticación JWT.
 * </p>
 */
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * Envía una solicitud de seguimiento a otro usuario.
     * <p>Endpoint: {@code POST /api/follows?targetId=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario actual
     * @param targetId       ID del usuario a seguir
     * @return HTTP 200 con mensaje de confirmación
     */
    @PostMapping
    public ResponseEntity<String> followUser(
            Authentication authentication,
            @RequestParam Long targetId) {

        Long userId = (Long) authentication.getPrincipal();
        followService.followUser(userId, targetId);
        return ResponseEntity.ok("Solicitud de seguimiento enviada/aceptada");
    }

    /**
     * Deja de seguir a un usuario.
     * <p>Endpoint: {@code DELETE /api/follows?targetId=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario actual
     * @param targetId       ID del usuario a dejar de seguir
     * @return HTTP 200 con mensaje de confirmación
     */
    @DeleteMapping
    public ResponseEntity<String> unfollowUser(
            Authentication authentication,
            @RequestParam Long targetId) {

        Long userId = (Long) authentication.getPrincipal();
        followService.unfollowUser(userId, targetId);
        return ResponseEntity.ok("Dejado de seguir correctamente");
    }

    /**
     * Obtiene las solicitudes de seguimiento pendientes recibidas por el usuario.
     * <p>Endpoint: {@code GET /api/follows/pending}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 con la lista de solicitudes pendientes
     */
    @GetMapping("/pending")
    public ResponseEntity<List<FollowRequestResponse>> getPendingRequests(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(followService.getPendingRequests(userId));
    }

    /**
     * Obtiene el n?mero de solicitudes de seguimiento pendientes sin cargar sus datos.
     * <p>Endpoint: {@code GET /api/follows/pending-count}</p>
     *
     * @param authentication contexto de autenticaci?n con el ID del usuario
     * @return HTTP 200 con el n?mero de solicitudes pendientes
     */
    @GetMapping("/pending-count")
    public ResponseEntity<Long> getPendingRequestsCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(followService.getPendingRequestsCount(userId));
    }

    /**
     * Acepta una solicitud de seguimiento pendiente.
     * <p>Endpoint: {@code POST /api/follows/accept?followerId=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario que acepta
     * @param followerId     ID del usuario que envió la solicitud
     * @return HTTP 200 con mensaje de confirmación
     */
    @PostMapping("/accept")
    public ResponseEntity<String> acceptFollow(
            Authentication authentication,
            @RequestParam Long followerId) {

        Long userId = (Long) authentication.getPrincipal();
        followService.acceptFollowRequest(userId, followerId);
        return ResponseEntity.ok("Solicitud de seguimiento aceptada");
    }

    /**
     * Rechaza una solicitud de seguimiento pendiente.
     * <p>Endpoint: {@code POST /api/follows/reject?followerId=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario que rechaza
     * @param followerId     ID del usuario que envió la solicitud
     * @return HTTP 200 con mensaje de confirmación
     */
    @PostMapping("/reject")
    public ResponseEntity<String> rejectFollow(
            Authentication authentication,
            @RequestParam Long followerId) {

        Long userId = (Long) authentication.getPrincipal();
        followService.rejectFollowRequest(userId, followerId);
        return ResponseEntity.ok("Solicitud de seguimiento rechazada");
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<UserSuggestionResponse>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowersSummary(userId));
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<UserSuggestionResponse>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowingSummary(userId));
    }
}
