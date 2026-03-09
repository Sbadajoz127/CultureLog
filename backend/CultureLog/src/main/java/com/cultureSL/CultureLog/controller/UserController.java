package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
