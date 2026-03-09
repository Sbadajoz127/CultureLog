package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.exception.BadRequestException;
import com.cultureSL.CultureLog.exception.ResourceNotFoundException;
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
 * Implementación del servicio de gestión de usuarios.
 * <p>
 * Gestiona el registro con cifrado de contraseña, autenticación,
 * actualización de perfil y el flujo completo de recuperación de contraseña
 * mediante tokens temporales enviados por email.
 * </p>
 *
 * @see UserService
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    /** {@inheritDoc} */
    @Override
    public User registerUser(User user) throws Exception {
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
    public void requestPasswordReset(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No existe ningún usuario con ese email"));

        tokenRepository.deleteByUser(user);

        PasswordResetToken token = new PasswordResetToken(user);
        tokenRepository.save(token);

        emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) throws Exception {
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        user.setProfilePictureUrl(imageUrl);
        userRepository.save(user);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void removeProfilePicture(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setProfilePictureUrl(null);
        userRepository.save(user);
    }
}
