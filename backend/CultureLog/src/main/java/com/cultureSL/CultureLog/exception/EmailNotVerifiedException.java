package com.cultureSL.CultureLog.exception;

/**
 * Excepción lanzada cuando un usuario intenta iniciar sesión sin haber verificado su correo.
 * <p>
 * Es capturada por {@link GlobalExceptionHandler} y devuelta como HTTP 403 Forbidden
 * con un indicador adicional {@code emailNotVerified: true}.
 * </p>
 */
public class EmailNotVerifiedException extends RuntimeException {

    /**
     * Crea una nueva excepción de correo no verificado.
     *
     * @param message descripción del motivo
     */
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
