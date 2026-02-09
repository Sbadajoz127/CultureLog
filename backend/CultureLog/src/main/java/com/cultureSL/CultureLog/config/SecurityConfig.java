package com.cultureSL.CultureLog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Clase de configuración de Spring Security.
 * <p>
 * Define las reglas de seguridad de la aplicación, como el cifrado de contraseñas
 * y los permisos de acceso a los endpoints HTTP.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define el bean para la encriptación de contraseñas.
     * Utiliza BCrypt, un algoritmo de hashing seguro y robusto.
     *
     * @return Instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     * <p>
     * Configuración actual:
     * <ul>
     * <li>Deshabilita CSRF (adecuado para APIs REST stateless).</li>
     * <li>Permite acceso público (permitAll) a todos los endpoints.
     * <strong>Nota:</strong> Esto debe restringirse en un entorno de producción real.</li>
     * </ul>
     *
     * @param http Objeto {@link HttpSecurity} para configurar la seguridad web.
     * @return La cadena de filtros construida.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // TODO: En producción esto se cambia
            );
        return http.build();
    }
}