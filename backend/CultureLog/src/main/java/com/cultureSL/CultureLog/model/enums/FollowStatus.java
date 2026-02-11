package com.cultureSL.CultureLog.model.enums;

/**
 * Define los estados posibles de una relación de seguimiento entre usuarios.
 */
public enum FollowStatus {
    /**
     * La solicitud ha sido enviada pero el usuario destino (Perfil Privado) aún no la ha aprobado.
     * El contenido no es visible todavía.
     */
    PENDING,
    /**
     * La relación ha sido aprobada y es activa. El seguidor puede ver el contenido del seguido.
     */
    ACCEPTED,
    /**
     * El usuario ha sido bloqueado y no puede interactuar ni ver el perfil.
     */
    BLOCKED
}