package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.UserSettings;
import com.cultureSL.CultureLog.model.enums.AppTheme;
import com.cultureSL.CultureLog.model.enums.ProfilePrivacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la configuración del usuario.
 * Evita exponer el ID interno y la relación JPA de la entidad UserSettings.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSettingsResponse {
    private ProfilePrivacy profilePrivacy;
    private boolean showFutureList;
    private boolean allowComments;
    private AppTheme theme;
    private String accentColor;
    private boolean emailNotifications;

    public static UserSettingsResponse from(UserSettings settings) {
        if (settings == null) return new UserSettingsResponse();
        return UserSettingsResponse.builder()
                .profilePrivacy(settings.getProfilePrivacy())
                .showFutureList(settings.isShowLibrary())
                .allowComments(settings.isAllowComments())
                .theme(settings.getTheme())
                .accentColor(settings.getAccentColor())
                .emailNotifications(settings.isEmailNotifications())
                .build();
    }
}
