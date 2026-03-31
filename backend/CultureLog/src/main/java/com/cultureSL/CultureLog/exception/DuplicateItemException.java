package com.cultureSL.CultureLog.exception;

/**
 * Excepción lanzada cuando se intenta añadir un ítem que ya existe en la biblioteca del usuario.
 * <p>
 * Es capturada por {@link GlobalExceptionHandler} y devuelta como HTTP 409 Conflict.
 * </p>
 */
public class DuplicateItemException extends RuntimeException {

    /**
     * Crea una nueva excepción de ítem duplicado.
     *
     * @param message descripción del conflicto
     */
    public DuplicateItemException(String message) {
        super(message);
    }
}
