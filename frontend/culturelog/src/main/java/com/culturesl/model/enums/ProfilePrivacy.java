package com.culturesl.model.enums;

/**
 * Enumeración que define los niveles de visibilidad del perfil de un usuario.
 */
public enum ProfilePrivacy {
    /** El perfil y los posts son visibles para todo el mundo. */
    PUBLICO,
    /** El contenido solo es visible para seguidores aceptados. */
    SOLO_AMIGOS, // Nota: En la implementación actual suele comportarse como PRIVADO o requerir lógica extra.
    /** El perfil está cerrado y requiere solicitud de seguimiento para ver contenido. */
    PRIVADO
}