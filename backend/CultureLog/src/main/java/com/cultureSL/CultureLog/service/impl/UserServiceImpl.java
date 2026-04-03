package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.exception.BadRequestException;
import com.cultureSL.CultureLog.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import com.cultureSL.CultureLog.dto.UserSettingsRequest;
import com.cultureSL.CultureLog.model.PasswordResetToken;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.model.UserSettings;
import com.cultureSL.CultureLog.model.enums.AppTheme;
import com.cultureSL.CultureLog.model.enums.ProfilePrivacy;
import com.cultureSL.CultureLog.repository.PasswordResetTokenRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.EmailService;
import com.cultureSL.CultureLog.service.UserService;
import com.cultureSL.CultureLog.service.ImageStorageService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de usuarios.
 * <p>
 * Gestiona el registro con cifrado de contraseña, autenticación,
 * actualización de perfil y el flujo completo de recuperación de contraseña
 * mediante tokens temporales enviados por email.
 * </p>
 *
 * @see UserService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String CLOUDINARY_HOST = "res.cloudinary.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final ImageStorageService imageStorageService;
    private final EntityManager entityManager;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public boolean exists(String username) {
        return userRepository.existsByUsername(username);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.debug("Solicitud de reset para email no registrado: {}", email);
            return;
        }

        User user = userOpt.get();
        tokenRepository.deleteByUser(user);
        entityManager.flush();

        PasswordResetToken token = new PasswordResetToken(user);
        tokenRepository.save(token);

        emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido o no encontrado"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new BadRequestException("El token ha expirado. Solicita uno nuevo.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void updateProfilePicture(Long userId, String imageUrl) {
        validateCloudinaryUrl(imageUrl);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getProfilePictureUrl() != null) {
            imageStorageService.deleteImage(user.getProfilePictureUrl());
        }

        user.setProfilePictureUrl(imageUrl);
        userRepository.save(user);
    }

    private void validateCloudinaryUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            if (!CLOUDINARY_HOST.equals(url.getHost())) {
                throw new BadRequestException("La URL de imagen debe pertenecer a Cloudinary");
            }
        } catch (MalformedURLException e) {
            throw new BadRequestException("La URL de imagen no es válida");
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void removeProfilePicture(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getProfilePictureUrl() != null) {
            imageStorageService.deleteImage(user.getProfilePictureUrl());
        }

        user.setProfilePictureUrl(null);
        userRepository.save(user);
    }

    /**
     * Actualiza la configuración global del usuario.
     *
     * @param userId  ID del usuario.
     * @param request DTO con las nuevas preferencias.
     * @return El objeto UserSettings actualizado.
     */
    @Override
    @Transactional
    public UserSettings updateSettings(Long userId, UserSettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserSettings settings = user.getSettings();

        // Si por algún error de base de datos antiguo fuera null, lo creamos
        if (settings == null) {
            settings = new UserSettings();
            settings.setUser(user);
            user.setSettings(settings);
        }

        // Actualizamos campos solo si no son nulos (o sobrescribimos todo según tu
        // lógica de UI)
        if (request.getProfilePrivacy() != null)
            settings.setProfilePrivacy(request.getProfilePrivacy());
        if (request.getTheme() != null)
            settings.setTheme(request.getTheme());
        if (request.getAccentColor() != null)
            settings.setAccentColor(request.getAccentColor());

        // Los booleanos primitivos siempre tienen valor (true/false), así que los
        // asignamos directamente
        settings.setShowFutureList(request.isShowFutureList());
        settings.setAllowComments(request.isAllowComments());
        settings.setEmailNotifications(request.isEmailNotifications());

        // Al guardar el usuario, se guardan los settings por el CascadeType.ALL
        userRepository.save(user);

        return settings;
    }

    /**
    * Recupera la configuración actual del usuario.
    * Útil para rellenar el formulario en el frontend antes de editar.
    *
    * @param userId ID del usuario.
    * @return Sus preferencias actuales.
    */
    @Override
    @Transactional(readOnly = true)
    public UserSettings getSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return user.getSettings();
    }
}
