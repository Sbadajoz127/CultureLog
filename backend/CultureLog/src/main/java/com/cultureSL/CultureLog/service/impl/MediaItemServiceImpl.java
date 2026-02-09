package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.dto.MediaItemRequest;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.repository.MediaItemRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.MediaItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación del servicio para la gestión de la biblioteca multimedia del usuario.
 * <p>
 * Permite realizar operaciones CRUD (Crear, Leer, Actualizar, Borrar) sobre los elementos
 * multimedia (Libros, Películas, etc.) asegurando que los usuarios solo modifiquen sus propios items.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class MediaItemServiceImpl implements MediaItemService {

    private final MediaItemRepository mediaItemRepository;
    private final UserRepository userRepository;

    /**
     * Recupera todos los items multimedia asociados a un usuario.
     *
     * @param userId ID del usuario propietario.
     * @return Lista completa de {@link MediaItem}.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MediaItem> getUserItems(Long userId) {
        return mediaItemRepository.findByUserId(userId);
    }

    /**
     * Filtra la biblioteca del usuario según el tipo de medio y/o el estado de consumo.
     *
     * @param userId ID del usuario.
     * @param type   Tipo de medio (ej. PELICULA, LIBRO) o {@code null} para ignorar filtro.
     * @param status Estado (ej. VISTO, POR_VER) o {@code null} para ignorar filtro.
     * @return Lista de items filtrada. Si ambos filtros son nulos, devuelve toda la lista.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MediaItem> filterItems(Long userId, MediaType type, MediaStatus status) {
        if (type != null && status != null) {
            return mediaItemRepository.findByUserIdAndType(userId, type).stream()
                    .filter(i -> i.getStatus() == status)
                    .toList();
        } else if (type != null) {
            return mediaItemRepository.findByUserIdAndType(userId, type);
        } else if (status != null) {
            return mediaItemRepository.findByUserIdAndStatus(userId, status);
        }
        return getUserItems(userId);
    }

    /**
     * Añade un nuevo ítem multimedia a la biblioteca del usuario.
     *
     * @param userId  ID del usuario que añade el ítem.
     * @param request DTO con los datos del nuevo ítem (título, tipo, rating, etc.).
     * @return El {@link MediaItem} persistido en base de datos con fecha de creación actual.
     * @throws RuntimeException Si el usuario no existe.
     */
    @Override
    @Transactional
    public MediaItem addItem(Long userId, MediaItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        MediaItem item = new MediaItem();
        item.setTitle(request.getTitle());
        item.setType(request.getType());
        item.setStatus(request.getStatus());
        item.setGenre(request.getGenre());
        item.setRating(request.getRating());
        item.setComment(request.getComment());
        item.setDateAdded(LocalDate.now());
        item.setUser(user);

        return mediaItemRepository.save(item);
    }

    /**
     * Actualiza los datos de un ítem existente.
     * <p>Incluye una validación de seguridad para asegurar que el ítem pertenece al usuario solicitante.</p>
     *
     * @param itemId  ID del ítem a modificar.
     * @param userId  ID del usuario que solicita la modificación.
     * @param request DTO con los nuevos datos.
     * @return El {@link MediaItem} actualizado.
     * @throws RuntimeException Si el ítem no existe o no pertenece al usuario.
     */
    @Override
    @Transactional
    public MediaItem updateItem(Long itemId, Long userId, MediaItemRequest request) {
        MediaItem item = mediaItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para editar este item");
        }

        item.setTitle(request.getTitle());
        item.setType(request.getType());
        item.setStatus(request.getStatus());
        item.setGenre(request.getGenre());
        item.setRating(request.getRating());
        item.setComment(request.getComment());

        return mediaItemRepository.save(item);
    }

    /**
     * Elimina un ítem de la base de datos.
     * <p>Incluye una validación de seguridad para asegurar que el ítem pertenece al usuario solicitante.</p>
     *
     * @param itemId ID del ítem a eliminar.
     * @param userId ID del usuario que solicita la eliminación.
     * @throws RuntimeException Si el ítem no existe o no pertenece al usuario.
     */
    @Override
    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        MediaItem item = mediaItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar este item");
        }

        mediaItemRepository.delete(item);
    }
}