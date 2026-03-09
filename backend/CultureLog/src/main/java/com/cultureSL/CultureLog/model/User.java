package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad central que representa a un usuario registrado en el sistema.
 * <p>
 * Gestiona las credenciales de acceso, la información personal y actúa como
 * propietario de la biblioteca multimedia y configuración.
 * </p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    /** Identificador único del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Nombre de usuario único para login e identificación en la red social. */
    @Column(nullable = false, unique = true)
    private String username;

    /** Contraseña encriptada. */
    @Column(nullable = false)
    private String password;

    /** Correo electrónico único para notificaciones y recuperación. */
    @Column(nullable = false)
    private String email;

    /** Url de la imagen de perfil del usuario. */
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    /**
     * Colección de obras multimedia añadidas por el usuario (Su biblioteca).
     * Si se borra el usuario, se borra su biblioteca (Cascade ALL).
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaItem> library = new ArrayList<>();

    /**
     * Configuración de preferencias del usuario (Tema, privacidad, etc.).
     * Relación 1 a 1.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserSettings settings;
}