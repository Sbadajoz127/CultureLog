package com.cultureSL.CultureLog.config;

import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.model.UserSettings;
import com.cultureSL.CultureLog.model.enums.AppTheme;
import com.cultureSL.CultureLog.model.enums.ProfilePrivacy;
import com.cultureSL.CultureLog.model.enums.Role;
import com.cultureSL.CultureLog.repository.PostRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

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

        int enabled = userRepository.enableAllExistingUsers();
        if (enabled > 0) {
            log.info("Activadas {} cuentas de usuarios existentes sin verificación previa", enabled);
        }

        int synced = postRepository.syncAllCommentCounts();
        if (synced > 0) {
            log.info("Sincronizados contadores de comentarios en {} publicaciones", synced);
        }

        fixTagsUniqueConstraint();
        seedAdminUser();
    }

    /**
     * Corrige la constraint única de la tabla tags: elimina cualquier índice único
     * que solo cubra la columna 'name' (global) y asegura que exista el índice
     * compuesto (user_id, name) que permite tags con el mismo nombre entre usuarios distintos.
     */
    private void fixTagsUniqueConstraint() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            boolean hasNameOnlyUnique = false;
            boolean hasCompositeUnique = false;
            String nameOnlyIndexName = null;

            try (ResultSet rs = meta.getIndexInfo(null, null, "tags", true, false)) {
                java.util.Map<String, java.util.List<String>> indexColumns = new java.util.LinkedHashMap<>();
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    String columnName = rs.getString("COLUMN_NAME");
                    if (indexName == null || "PRIMARY".equalsIgnoreCase(indexName)) continue;
                    indexColumns.computeIfAbsent(indexName, k -> new java.util.ArrayList<>()).add(columnName.toLowerCase());
                }

                for (var entry : indexColumns.entrySet()) {
                    java.util.List<String> cols = entry.getValue();
                    if (cols.size() == 1 && cols.contains("name")) {
                        hasNameOnlyUnique = true;
                        nameOnlyIndexName = entry.getKey();
                    }
                    if (cols.size() == 2 && cols.contains("user_id") && cols.contains("name")) {
                        hasCompositeUnique = true;
                    }
                }
            }

            if (hasNameOnlyUnique) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE tags DROP INDEX " + nameOnlyIndexName);
                    log.info("Eliminado índice único incorrecto '{}' (solo name) de la tabla tags", nameOnlyIndexName);
                }
            }

            if (!hasCompositeUnique) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE tags ADD CONSTRAINT uk_tags_user_name UNIQUE (user_id, name)");
                    log.info("Creado índice único compuesto (user_id, name) en la tabla tags");
                }
            }
        } catch (SQLException e) {
            log.warn("No se pudo verificar/corregir las constraints de la tabla tags: {}", e.getMessage());
        }
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
        admin.setEnabled(true);

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
