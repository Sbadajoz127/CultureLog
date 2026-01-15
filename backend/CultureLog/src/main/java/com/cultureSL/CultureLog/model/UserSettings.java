package com.cultureSL.CultureLog.model;

import com.cultureSL.CultureLog.model.enums.AppTheme;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfilePrivacy profilePrivacy = ProfilePrivacy.PUBLICO;

    @Column(nullable = false)
    private boolean showFutureList = true;

    @Column(nullable = false)
    private boolean allowComments = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppTheme theme = AppTheme.DARK;

    private String accentColor = "#448AFF"; 

    private boolean emailNotifications = true;
     
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}