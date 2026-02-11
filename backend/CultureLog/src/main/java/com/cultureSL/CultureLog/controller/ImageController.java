package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.service.impl.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST para la subida de imágenes a Cloudinary.
 * <p>
 * Permite subir imágenes al servicio Cloudinary y obtener sus URLs públicas.
 * </p>
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final CloudinaryService cloudinaryService;

    /**
     * Endpoint para subir una imagen a Cloudinary.
     * <p>Endpoint: {@code POST /api/images/upload}</p>
     *
     * @param file Archivo de imagen a subir.
     * @return URL pública de la imagen subida o mensaje de error si el archivo está vacío.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }
        String url = cloudinaryService.uploadImage(file);
        return ResponseEntity.ok(url);
    }
}