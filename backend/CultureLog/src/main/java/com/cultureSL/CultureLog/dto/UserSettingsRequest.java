package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.enums.AppTheme;
import com.cultureSL.CultureLog.model.enums.ProfilePrivacy;
import lombok.Data;

/**
 * DTO para la actualización de preferencias del usuario.
 * Agrupa todas las opciones configurables de la cuenta.
 */
@Data
public class UserSettingsRequest {
    private ProfilePrivacy profilePrivacy;
    private boolean showLibrary;
    private boolean allowComments;
    private AppTheme theme;
    private String accentColor;
    private boolean emailNotifications;
}