package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.config.JwtService;
import com.cultureSL.CultureLog.dto.AuthResponse;
import com.cultureSL.CultureLog.dto.LoginRequest;
import com.cultureSL.CultureLog.dto.RegisterRequest;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador REST para las operaciones de autenticación.
 * <p>
 * Expone endpoints públicos ({@code /api/auth/**}) para registro de usuarios,
 * inicio de sesión y recuperación de contraseña. Todos los endpoints de este
 * controlador están exentos de autenticación JWT.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>Endpoint: {@code POST /api/auth/register}</p>
     *
     * @param request datos de registro (username, password, email)
     * @return HTTP 201 con {@link AuthResponse} incluyendo el token JWT, o HTTP 400 si hay error
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword());
        newUser.setEmail(request.getEmail());

        User createdUser = userService.registerUser(newUser);
        String token = jwtService.generateToken(createdUser.getId(), createdUser.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(
                        createdUser.getId(),
                        createdUser.getUsername(),
                        createdUser.getEmail(),
                        token,
                        "Usuario registrado con éxito"
                ));
    }

    /**
     * Autentica a un usuario existente y devuelve un token JWT.
     * <p>Endpoint: {@code POST /api/auth/login}</p>
     *
     * @param request credenciales de acceso (username, password)
     * @return HTTP 200 con {@link AuthResponse} y token, o HTTP 401 si las credenciales son incorrectas
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.login(request.getUsername(), request.getPassword());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = jwtService.generateToken(user.getId(), user.getUsername());
            return ResponseEntity.ok(new AuthResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    token,
                    "Login correcto"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, null, "Credenciales incorrectas"));
        }
    }

    /**
     * Inicia el flujo de recuperación de contraseña enviando un email con un token.
     * <p>Endpoint: {@code POST /api/auth/request-reset?email=...}</p>
     *
     * @param email dirección de correo del usuario
     * @return HTTP 200 con mensaje de confirmación, o HTTP 400 si el email no existe
     */
    @PostMapping("/request-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        userService.requestPasswordReset(email);
        return ResponseEntity.ok("Correo de recuperación enviado");
    }

    /**
     * Restablece la contraseña de un usuario utilizando un token de seguridad.
     * <p>Endpoint: {@code POST /api/auth/reset-password?token=...&newPassword=...}</p>
     *
     * @param token       token de restablecimiento (UUID recibido por email)
     * @param newPassword nueva contraseña en texto plano
     * @return HTTP 200 con confirmación, o HTTP 400 si el token es inválido o ha expirado
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}
