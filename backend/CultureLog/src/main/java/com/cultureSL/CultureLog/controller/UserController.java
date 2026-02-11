package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de datos del usuario.
 * <p>
 * Maneja operaciones específicas del perfil de usuario que no están
 * relacionadas
 * con la autenticación, como la actualización de la foto de perfil.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Actualiza la foto de perfil de un usuario.
     * <p>
     * Endpoint: {@code POST /api/users/{userId}/profile-picture}
     * </p>
     *
     * @param userId   ID del usuario a actualizar.
     * @param imageUrl URL pública de la imagen (obtenida previamente de
     *                 Cloudinary).
     * @return {@code 200 OK} si la actualización fue exitosa.
     */
    @PostMapping("/{userId}/profile-picture")
    public ResponseEntity<Void> updateProfilePicture(
            @PathVariable Long userId,
            @RequestParam String imageUrl) {

        userService.updateProfilePicture(userId, imageUrl);
        return ResponseEntity.ok().build();
    }

    /**
     * Elimina la foto de perfil de un usuario, estableciendo la URL a null.
     * <p>
     * Endpoint: {@code DELETE /api/users/{userId}/profile-picture}
     * </p>
     *
     * @param userId ID del usuario a actualizar.
     * @return {@code 200 OK} si la eliminación fue exitosa.
     */
    @DeleteMapping("/{userId}/profile-picture")
    public ResponseEntity<Void> removeProfilePicture(@PathVariable Long userId) {
        userService.removeProfilePicture(userId);
        return ResponseEntity.ok().build();
    }
}