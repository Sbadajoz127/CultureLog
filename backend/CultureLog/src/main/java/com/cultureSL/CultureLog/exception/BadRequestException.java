package com.cultureSL.CultureLog.exception;

/**
 * Excepción lanzada cuando una solicitud del cliente contiene datos inválidos o incompletos.
 * <p>
 * Es capturada por {@link GlobalExceptionHandler} y devuelta como HTTP 400 Bad Request.
 * </p>
 */
public class BadRequestException extends RuntimeException {

    /**
     * Crea una nueva excepción de solicitud incorrecta.
     *
     * @param message descripción del error de validación
     */
    public BadRequestException(String message) {
        super(message);
    }
}
