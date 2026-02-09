package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.model.PasswordResetToken;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.model.UserSettings;
import com.cultureSL.CultureLog.model.enums.AppTheme;
import com.cultureSL.CultureLog.model.enums.ProfilePrivacy;
import com.cultureSL.CultureLog.repository.PasswordResetTokenRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.EmailService;
import com.cultureSL.CultureLog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementación del servicio de gestión de usuarios y autenticación.
 * <p>
 * Maneja el ciclo de vida de la cuenta de usuario: registro (con configuración por defecto),
 * inicio de sesión (verificación de credenciales) y flujo de recuperación de contraseña.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Realiza las siguientes acciones:
     * 1. Verifica que el username y email no existan ya.
     * 2. Encripta la contraseña.
     * 3. Crea e inicializa una entidad {@link UserSettings} con valores por defecto (Tema oscuro, perfil público).
     * 4. Vincula settings y usuario y persiste en base de datos.
     * </p>
     *
     * @param user Entidad usuario con los datos básicos.
     * @return El usuario registrado y guardado.
     * @throws Exception Si el nombre de usuario o email ya están en uso.
     */
    @Override
    public User registerUser(User user) throws Exception {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new Exception("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new Exception("El email ya está registrado");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        UserSettings defaultSettings = new UserSettings();
        defaultSettings.setTheme(AppTheme.DARK);
        defaultSettings.setProfilePrivacy(ProfilePrivacy.PUBLICO);
        defaultSettings.setAccentColor("#448AFF");

        defaultSettings.setUser(user); 
        user.setSettings(defaultSettings);

        return userRepository.save(user);
    }

    /**
     * Verifica las credenciales para iniciar sesión.
     *
     * @param username   Nombre de usuario.
     * @param rawPassword Contraseña en texto plano introducida por el usuario.
     * @return Un {@link Optional} que contiene el usuario si las credenciales son correctas, o vacío si fallan.
     */
    @Override
    public Optional<User> login(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    /**
     * Comprueba si un nombre de usuario ya existe en el sistema.
     *
     * @param username Nombre de usuario a verificar.
     * @return {@code true} si existe, {@code false} si está libre.
     */
    @Override
    public boolean exists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Inicia el proceso de restablecimiento de contraseña.
     * <p>
     * Genera un token de seguridad, elimina tokens antiguos del usuario y envía
     * un correo electrónico con el nuevo token.
     * </p>
     *
     * @param email Correo electrónico del usuario.
     * @throws Exception Si no existe ningún usuario asociado a ese email.
     */
    @Override
    @Transactional
    public void requestPasswordReset(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("No existe ningún usuario con ese email"));

        tokenRepository.deleteByUser(user);

        PasswordResetToken token = new PasswordResetToken(user);
        tokenRepository.save(token);

        emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
    }

    /**
     * Finaliza el proceso de restablecimiento de contraseña.
     * <p>
     * Valida el token proporcionado y, si es correcto y no ha expirado,
     * actualiza la contraseña del usuario (encriptándola) y consume el token.
     * </p>
     *
     * @param token       Token de seguridad recibido por correo.
     * @param newPassword Nueva contraseña en texto plano.
     * @throws Exception Si el token es inválido, no existe o ha expirado.
     */
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) throws Exception {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new Exception("Token inválido o no encontrado"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new Exception("El token ha expirado. Solicita uno nuevo.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}