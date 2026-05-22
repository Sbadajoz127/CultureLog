package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.enums.AppTheme;
import com.cultureSL.CultureLog.model.enums.ProfilePrivacy;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para la actualización de preferencias del usuario.
 * Agrupa todas las opciones configurables de la cuenta.
 */
@Data
public class UserSettingsRequest {
    private ProfilePrivacy profilePrivacy;
    private boolean showFutureList;
    private boolean allowComments;
    private AppTheme theme;
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe tener formato hexadecimal (#RRGGBB)")
    private String accentColor;
    private boolean emailNotifications;
}