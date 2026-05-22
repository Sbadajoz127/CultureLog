package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Controlador REST para la subida de imágenes a Cloudinary.
 * <p>
 * Permite subir imágenes al servicio Cloudinary y obtener sus URLs públicas.
 * Valida tanto el Content-Type declarado como los magic bytes reales del archivo.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final ImageStorageService imageStorageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("El archivo excede el tamaño máximo de 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body("Tipo de archivo no permitido. Solo se aceptan: JPEG, PNG, GIF, WEBP");
        }

        if (!isValidImageMagicBytes(file)) {
            return ResponseEntity.badRequest().body("El contenido del archivo no corresponde a una imagen válida");
        }

        String url = imageStorageService.uploadImage(file);
        return ResponseEntity.ok(url);
    }

    /**
     * Verifica los magic bytes del archivo para confirmar que es realmente una imagen.
     * Previene ataques donde se envía un archivo malicioso con Content-Type falsificado.
     */
    private boolean isValidImageMagicBytes(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header);
            if (bytesRead < 4) return false;

            // JPEG: FF D8 FF
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                return true;
            }
            // PNG: 89 50 4E 47 0D 0A 1A 0A
            if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                return true;
            }
            // GIF: 47 49 46 38
            if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) {
                return true;
            }
            // WEBP: RIFF....WEBP
            if (bytesRead >= 12
                    && header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46
                    && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
                return true;
            }

            return false;
        } catch (IOException e) {
            log.warn("Error leyendo magic bytes del archivo subido", e);
            return false;
        }
    }
}