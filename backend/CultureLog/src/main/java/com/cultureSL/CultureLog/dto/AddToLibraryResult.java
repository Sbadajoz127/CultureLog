package com.cultureSL.CultureLog.dto;

/**
 * Resultado de añadir un ítem desde búsqueda externa: creación nueva o ítem ya existente (idempotente).
 */
public record AddToLibraryResult(boolean created, MediaItemResponse item) {}
