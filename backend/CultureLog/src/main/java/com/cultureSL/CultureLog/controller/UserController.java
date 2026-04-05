package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.UserProfileResponse;
import com.cultureSL.CultureLog.dto.UserSettingsRequest;
import com.cultureSL.CultureLog.dto.UserSuggestionResponse;
import com.cultureSL.CultureLog.model.UserSettings;
import com.cultureSL.CultureLog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión del perfil de usuario.
 * <p>
 * Permite actualizar y eliminar la foto de perfil del usuario autenticado.
 * Requiere autenticación JWT.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Devuelve una lista de usuarios sugeridos para seguir (máx. 5).
     * <p>Endpoint: {@code GET /api/users/suggestions}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 con la lista de sugerencias
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<UserSuggestionResponse>> getSuggestions(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getSuggestedUsers(userId));
    }

    /**
     * Actualiza la foto de perfil del usuario autenticado.
     * <p>Endpoint: {@code PUT /api/users/profile-picture?imageUrl=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param imageUrl       nueva URL de la imagen de perfil
     * @return HTTP 200 sin contenido
     */
    @PutMapping("/profile-picture")
    public ResponseEntity<Void> updateProfilePicture(
            Authentication authentication,
            @RequestParam String imageUrl) {

        Long userId = (Long) authentication.getPrincipal();
        userService.updateProfilePicture(userId, imageUrl);
        return ResponseEntity.ok().build();
    }

    /**
     * Elimina la foto de perfil del usuario autenticado.
     * <p>Endpoint: {@code DELETE /api/users/profile-picture}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 sin contenido
     */
    @DeleteMapping("/profile-picture")
    public ResponseEntity<Void> removeProfilePicture(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.removeProfilePicture(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene el perfil público de un usuario con stats, posts y biblioteca (según privacidad).
     * <p>Endpoint: {@code GET /api/users/{userId}/profile}</p>
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable Long userId,
            Authentication authentication) {
        Long viewerId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserProfile(userId, viewerId));
    }

    /**
     * Obtiene la configuración actual del usuario.
     * <p>Endpoint: {@code GET /api/users/{userId}/settings}</p>
     * * @param userId ID del usuario.
     * @return Objeto UserSettings con las preferencias.
     */
    @GetMapping("/{userId}/settings")
    public ResponseEntity<UserSettings> getSettings(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getSettings(userId));
    }

    /**
     * Actualiza la configuración del usuario.
     * <p>Endpoint: {@code PUT /api/users/{userId}/settings}</p>
     * * @param userId  ID del usuario.
     * @param request JSON con los nuevos valores.
     * @return La configuración actualizada.
     */
    @PutMapping("/{userId}/settings")
    public ResponseEntity<UserSettings> updateSettings(
            @PathVariable Long userId,
            @RequestBody UserSettingsRequest request) {
        return ResponseEntity.ok(userService.updateSettings(userId, request));
    }
}
