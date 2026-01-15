package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.model.ProfilePrivacy;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.model.UserSettings;
import com.cultureSL.CultureLog.model.enums.AppTheme;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    @Override
    public boolean exists(String username) {
        return userRepository.existsByUsername(username);
    }
}