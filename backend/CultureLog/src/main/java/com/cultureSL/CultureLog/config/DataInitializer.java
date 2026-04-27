package com.cultureSL.CultureLog.config;

import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.model.UserSettings;
import com.cultureSL.CultureLog.model.enums.AppTheme;
import com.cultureSL.CultureLog.model.enums.ProfilePrivacy;
import com.cultureSL.CultureLog.model.enums.Role;
import com.cultureSL.CultureLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:#{null}}")
    private String adminUsername;

    @Value("${admin.password:#{null}}")
    private String adminPassword;

    @Value("${admin.email:#{null}}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int updated = userRepository.setDefaultCreatedAtWhereNull();
        if (updated > 0) {
            log.info("Asignada fecha de registro (NOW) a {} usuarios existentes sin created_at", updated);
        }

        seedAdminUser();
    }

    private void seedAdminUser() {
        if (adminUsername == null || adminPassword == null || adminEmail == null) {
            log.debug("Variables ADMIN_USERNAME/ADMIN_PASSWORD/ADMIN_EMAIL no configuradas; se omite la creación del admin.");
            return;
        }

        if (userRepository.findByUsername(adminUsername).isPresent()) {
            log.debug("El usuario admin '{}' ya existe, no se crea de nuevo.", adminUsername);
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEmail(adminEmail);
        admin.setRole(Role.ADMIN);

        UserSettings settings = new UserSettings();
        settings.setTheme(AppTheme.DARK);
        settings.setProfilePrivacy(ProfilePrivacy.PUBLICO);
        settings.setAccentColor("#448AFF");
        settings.setUser(admin);
        admin.setSettings(settings);

        userRepository.save(admin);
        log.info("Usuario administrador '{}' creado con éxito.", adminUsername);
    }
}
