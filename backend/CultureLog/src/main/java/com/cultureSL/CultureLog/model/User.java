package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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