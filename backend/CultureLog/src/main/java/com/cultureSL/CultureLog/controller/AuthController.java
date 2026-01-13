package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.AuthResponse;
import com.cultureSL.CultureLog.dto.LoginRequest;
import com.cultureSL.CultureLog.dto.RegisterRequest;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPassword(request.getPassword());
            newUser.setEmail(request.getEmail());

            User createdUser = userService.registerUser(newUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(
                            createdUser.getId(),
                            createdUser.getUsername(),
                            createdUser.getEmail(),
                            "Usuario registrado con éxito"
                    ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, null, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.login(request.getUsername(), request.getPassword());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return ResponseEntity.ok(new AuthResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    "Login correcto"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, "Credenciales incorrectas"));
        }
    }
}