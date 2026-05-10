package com.cultureSL.CultureLog.model.enums;

/**
 * Enumeración que define los niveles de visibilidad del perfil de un usuario.
 */
public enum ProfilePrivacy {
    /** El perfil y los posts son visibles para todo el mundo. */
    PUBLICO,
    /** El contenido solo es visible para amigos mutuos (seguimiento recíproco con estado ACCEPTED). */
    SOLO_AMIGOS,
    /** El perfil está cerrado y requiere solicitud de seguimiento para ver contenido. */
    PRIVADO
}