package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.UserProfileResponse;
import com.cultureSL.CultureLog.dto.UserSettingsRequest;
import com.cultureSL.CultureLog.dto.UserSettingsResponse;
import com.cultureSL.CultureLog.dto.UserSuggestionResponse;
import com.cultureSL.CultureLog.model.UserSettings;
import com.cultureSL.CultureLog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * Busca usuarios por nombre de usuario.
     * <p>Endpoint: {@code GET /api/users/search?q=termino&page=0&size=10}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param q              término de búsqueda
     * @param page           número de página (por defecto 0)
     * @param size           tamaño de página (por defecto 10)
     * @return HTTP 200 con la página de usuarios encontrados
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserSuggestionResponse>> searchUsers(
            Authentication authentication,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "username"));
        return ResponseEntity.ok(userService.searchUsers(q, userId, pageable));
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
     * <p>Endpoint: {@code GET /api/users/profile/{username}}</p>
     */
    @GetMapping("/profile/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable String username,
            Authentication authentication) {
        Long viewerId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserProfileByUsername(username, viewerId));
    }

    /**
     * Obtiene la configuración actual del usuario autenticado.
     * <p>Endpoint: {@code GET /api/users/settings}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @return Objeto UserSettings con las preferencias.
     */
    @GetMapping("/settings")
    public ResponseEntity<UserSettingsResponse> getSettings(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserSettings settings = userService.getSettings(userId);
        return ResponseEntity.ok(UserSettingsResponse.from(settings));
    }

    @PutMapping("/settings")
    public ResponseEntity<UserSettingsResponse> updateSettings(
            Authentication authentication,
            @Valid @RequestBody UserSettingsRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        UserSettings settings = userService.updateSettings(userId, request);
        return ResponseEntity.ok(UserSettingsResponse.from(settings));
    }

    /**
     * Solicita la eliminación de la cuenta, enviando un código de confirmación por email.
     * <p>Endpoint: {@code POST /api/users/request-deletion}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 si se envió el código correctamente
     */
    @PostMapping("/request-deletion")
    public ResponseEntity<Void> requestAccountDeletion(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.requestAccountDeletion(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Confirma la eliminación de la cuenta usando el código recibido por email.
     * <p>Endpoint: {@code DELETE /api/users/confirm-deletion?code=123456}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param code           código de 6 dígitos recibido por email
     * @return HTTP 200 si la cuenta se eliminó correctamente
     */
    @DeleteMapping("/confirm-deletion")
    public ResponseEntity<Void> confirmAccountDeletion(
            Authentication authentication,
            @RequestParam String code) {
        Long userId = (Long) authentication.getPrincipal();
        userService.confirmAccountDeletion(userId, code);
        return ResponseEntity.ok().build();
    }

    /**
     * Actualiza el banner del usuario autenticado.
     * <p>Endpoint: {@code PUT /api/users/banner?imageUrl=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param imageUrl       nueva URL de la imagen del banner
     * @return HTTP 200 sin contenido
     */
    @PutMapping("/banner")
    public ResponseEntity<Void> updateBanner(
            Authentication authentication,
            @RequestParam String imageUrl) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updateBanner(userId, imageUrl);
        return ResponseEntity.ok().build();
    }

    /**
     * Elimina el banner del usuario autenticado.
     * <p>Endpoint: {@code DELETE /api/users/banner}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 sin contenido
     */
    @DeleteMapping("/banner")
    public ResponseEntity<Void> removeBanner(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.removeBanner(userId);
        return ResponseEntity.ok().build();
    }
}
