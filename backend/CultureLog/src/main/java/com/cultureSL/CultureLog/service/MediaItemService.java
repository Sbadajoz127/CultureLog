package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.dto.MediaItemRequest;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;

import java.util.List;

/**
 * Servicio que gestiona la biblioteca multimedia personal de cada usuario.
 * <p>
 * Define el contrato para las operaciones CRUD sobre los ítems multimedia
 * y el filtrado por tipo y estado de consumo.
 * </p>
 */
public interface MediaItemService {

    /**
     * Obtiene todos los ítems multimedia de la biblioteca de un usuario.
     *
     * @param userId ID del usuario propietario
     * @return lista completa de ítems del usuario
     */
    List<MediaItem> getUserItems(Long userId);

    /**
     * Filtra los ítems de la biblioteca de un usuario por tipo y/o estado.
     * <p>
     * Si alguno de los filtros es {@code null}, se ignora y no se aplica.
     * </p>
     *
     * @param userId ID del usuario propietario
     * @param type   tipo de medio (puede ser {@code null})
     * @param status estado de consumo (puede ser {@code null})
     * @return lista de ítems que coinciden con los filtros aplicados
     */
    List<MediaItem> filterItems(Long userId, MediaType type, MediaStatus status);

    /**
     * Añade un nuevo ítem multimedia a la biblioteca del usuario.
     *
     * @param userId  ID del usuario propietario
     * @param request datos del ítem a crear
     * @return el ítem creado con su ID generado
     */
    MediaItem addItem(Long userId, MediaItemRequest request);

    /**
     * Actualiza los datos de un ítem multimedia existente.
     *
     * @param itemId  ID del ítem a actualizar
     * @param userId  ID del usuario propietario (para validación de pertenencia)
     * @param request nuevos datos del ítem
     * @return el ítem actualizado
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si el ítem no existe
     * @throws com.cultureSL.CultureLog.exception.UnauthorizedException     si el ítem no pertenece al usuario
     */
    MediaItem updateItem(Long itemId, Long userId, MediaItemRequest request);

    /**
     * Elimina la imagen asociada a un ítem multimedia.
     *
     * @param itemId ID del ítem
     * @param userId ID del usuario propietario (para validación de pertenencia)
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si el ítem no existe
     * @throws com.cultureSL.CultureLog.exception.UnauthorizedException     si el ítem no pertenece al usuario
     */
    void removeMediaItemImage(Long itemId, Long userId);

    /**
     * Elimina un ítem multimedia de la biblioteca del usuario.
     *
     * @param itemId ID del ítem a eliminar
     * @param userId ID del usuario propietario (para validación de pertenencia)
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si el ítem no existe
     * @throws com.cultureSL.CultureLog.exception.UnauthorizedException     si el ítem no pertenece al usuario
     */
    void deleteItem(Long itemId, Long userId);
}
