package com.cultureSL.CultureLog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ligero para representar una sugerencia de usuario a seguir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSuggestionResponse {
    private Long id;
    private String username;
    private String profilePictureUrl;
}
