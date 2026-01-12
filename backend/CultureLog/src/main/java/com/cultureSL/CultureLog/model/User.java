package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data // Genera Getters, Setters, ToString, Equals, HashCode
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // Aquí guardaremos la contraseña encriptada más adelante

    @Column(nullable = false)
    private String email;

    // Relación: Un usuario tiene muchos Items Multimedia
    // cascade = ALL: Si borras al usuario, se borran sus items
    // orphanRemoval = true: Si quitas un item de la lista, se borra de la BD
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaItem> library = new ArrayList<>();
}