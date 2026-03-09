package com.cultureSL.CultureLog.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado en la base de datos.
 * <p>
 * Es capturada por {@link GlobalExceptionHandler} y devuelta como HTTP 404 Not Found.
 * </p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Crea una nueva excepción de recurso no encontrado.
     *
     * @param message descripción indicando qué recurso no se encontró
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
