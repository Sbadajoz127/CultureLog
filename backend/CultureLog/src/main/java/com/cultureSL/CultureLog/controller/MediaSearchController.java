package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.AddToLibraryResult;
import com.cultureSL.CultureLog.dto.MediaItemResponse;
import com.cultureSL.CultureLog.dto.search.MediaSearchResult;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.service.search.MediaSearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la búsqueda de contenido multimedia en APIs externas.
 * <p>
 * Permite buscar películas, series, libros, anime, manga y videojuegos en
 * fuentes externas (TMDB, Open Library, Jikan, RAWG) y añadir los resultados
 * a la biblioteca personal del usuario. Requiere autenticación JWT.
 * </p>
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
public class MediaSearchController {

    private final MediaSearchService mediaSearchService;

    /**
     * Busca contenido multimedia en las APIs externas configuradas.
     * <p>Endpoint: {@code GET /api/search?query=...&type=...&page=0}</p>
     *
     * @param query texto de búsqueda (obligatorio)
     * @param type  filtro opcional por tipo de medio
     * @param page  número de página de resultados (por defecto 0)
     * @return HTTP 200 con la lista de resultados normalizados en formato {@link MediaSearchResult}
     */
    @GetMapping
    public ResponseEntity<List<MediaSearchResult>> search(
            Authentication authentication,
            @RequestParam @NotBlank(message = "La consulta de búsqueda es obligatoria") @Size(max = 200, message = "La consulta no puede exceder 200 caracteres") String query,
            @RequestParam(required = false) MediaType type,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "La página no puede ser negativa") int page) {

        List<MediaSearchResult> results = mediaSearchService.search(query, type, page);
        Long userId = (Long) authentication.getPrincipal();
        List<MediaSearchResult> enriched = mediaSearchService.enrichWithLibraryStatus(userId, results);
        return ResponseEntity.ok(enriched);
    }

    /**
     * Añade un resultado de búsqueda externa a la biblioteca del usuario autenticado.
     * <p>Endpoint: {@code POST /api/search/add-to-library}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param searchResult   resultado de búsqueda a convertir en ítem de biblioteca
     * @return HTTP 201 si se creó el ítem; HTTP 200 si ya existía (mismo cuerpo {@link MediaItemResponse})
     */
    @PostMapping("/add-to-library")
    public ResponseEntity<MediaItemResponse> addToLibrary(
            Authentication authentication,
            @Valid @RequestBody MediaSearchResult searchResult) {

        Long userId = (Long) authentication.getPrincipal();
        AddToLibraryResult result = mediaSearchService.addToLibrary(userId, searchResult);
        MediaItemResponse body = result.item();
        if (result.created()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        }
        return ResponseEntity.ok(body);
    }
}
