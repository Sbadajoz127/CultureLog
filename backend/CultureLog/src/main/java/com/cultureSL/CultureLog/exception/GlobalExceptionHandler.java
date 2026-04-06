package com.cultureSL.CultureLog.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para todos los controladores REST.
 * <p>
 * Intercepta las excepciones lanzadas durante el procesamiento de peticiones HTTP
 * y las convierte en respuestas JSON estandarizadas con timestamp, código de estado,
 * tipo de error y mensaje descriptivo.
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de recurso no encontrado.
     *
     * @param ex excepción capturada
     * @return respuesta HTTP 404 con detalle del error
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Maneja excepciones de acceso no autorizado.
     *
     * @param ex excepción capturada
     * @return respuesta HTTP 403 con detalle del error
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Maneja excepciones de solicitud incorrecta.
     *
     * @param ex excepción capturada
     * @return respuesta HTTP 400 con detalle del error
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Maneja excepciones de recurso duplicado.
     *
     * @param ex excepción capturada
     * @return respuesta HTTP 409 con detalle del conflicto
     */
    @ExceptionHandler(DuplicateItemException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateItemException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Maneja errores de validación de {@code @Valid} en los DTOs de entrada.
     * <p>
     * Recopila todos los errores de campo y los devuelve como un mensaje concatenado.
     * </p>
     *
     * @param ex excepción de validación de argumentos
     * @return respuesta HTTP 400 con los campos y mensajes de error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, errors);
    }

    /**
     * Maneja violaciones de restricciones en parámetros de método ({@code @Validated} en controladores).
     *
     * @param ex excepción de violación de restricciones
     * @return respuesta HTTP 400 con los mensajes de violación
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, errors);
    }

    /**
     * Maneja métodos HTTP no soportados por el endpoint.
     *
     * @param ex excepción de método no permitido
     * @return respuesta HTTP 405 con detalle
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    /**
     * Manejador genérico para cualquier excepción no capturada específicamente.
     *
     * @param ex excepción no controlada
     * @return respuesta HTTP 500 con mensaje genérico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Error no controlado", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    /**
     * Construye una respuesta de error estandarizada en formato JSON.
     *
     * @param status  código de estado HTTP
     * @param message mensaje descriptivo del error
     * @return entidad de respuesta con el cuerpo formateado
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
