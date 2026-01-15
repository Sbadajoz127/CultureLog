package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.cultureSL.CultureLog.model.enums.FollowStatus;

@Entity
@Table(name = "follows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "followed_id"}) // Evita seguir dos veces al mismo
})
@Data
@NoArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quién envía la solicitud (El seguidor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    // A quién quieren seguir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;

    // Estado: PENDING (si el perfil es privado), ACCEPTED (si es público o ya aceptó)
    @Enumerated(EnumType.STRING)
    private FollowStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();
}