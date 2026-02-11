package com.cultureSL.CultureLog.model;

import com.cultureSL.CultureLog.model.enums.AppTheme;
import com.cultureSL.CultureLog.model.enums.ProfilePrivacy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que almacena las preferencias de configuración y personalización del usuario.
 * <p>
 * Gestiona tanto aspectos de privacidad (quién ve qué) como aspectos visuales
 * de la interfaz (tema, colores). Tiene una relación 1 a 1 obligatoria con el Usuario.
 * </p>
 */
@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nivel de privacidad del perfil.
     * Determina quién puede ver las publicaciones y seguir al usuario automáticamente.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfilePrivacy profilePrivacy = ProfilePrivacy.PUBLICO;

    /**
     * Indica si la lista de items "Por Ver" (Pendientes) es visible para otros usuarios.
     * Útil si el usuario quiere compartir lo que ha visto, pero mantener en privado sus planes futuros.
     */
    @Column(nullable = false)
    private boolean showFutureList = true;

    /**
     * Interruptor global para permitir o bloquear comentarios en las publicaciones del usuario.
     */
    @Column(nullable = false)
    private boolean allowComments = true;

    // --- APARIENCIA ---

    /**
     * Tema visual preferido para la aplicación (Claro/Oscuro).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppTheme theme = AppTheme.DARK;

    /**
     * Código Hexadecimal del color de acento (ej: botones, enlaces).
     * Permite al usuario dar un toque personal a su interfaz.
     */
    private String accentColor = "#448AFF"; 

    // --- NOTIFICACIONES ---

    /**
     * Indica si el usuario desea recibir correos electrónicos cuando ocurren eventos (ej: nuevos seguidores).
     */
    private boolean emailNotifications = true;
     
    /**
     * Usuario propietario de esta configuración.
     * Relación Uno-a-Uno.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}