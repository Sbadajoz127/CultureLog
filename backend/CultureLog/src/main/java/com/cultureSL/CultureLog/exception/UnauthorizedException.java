package com.cultureSL.CultureLog.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder a un recurso sin los permisos necesarios.
 * <p>
 * Es capturada por {@link GlobalExceptionHandler} y devuelta como HTTP 403 Forbidden.
 * </p>
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Crea una nueva excepción de acceso no autorizado.
     *
     * @param message descripción del motivo de la denegación
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
