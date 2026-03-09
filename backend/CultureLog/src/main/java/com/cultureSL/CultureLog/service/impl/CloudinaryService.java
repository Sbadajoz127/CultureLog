package com.cultureSL.CultureLog.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cultureSL.CultureLog.exception.BadRequestException;
import com.cultureSL.CultureLog.service.ImageStorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la integración con Cloudinary.
 * <p>
 * Proporciona métodos para subir y eliminar imágenes en Cloudinary,
 * gestionando el ciclo de vida completo de los recursos multimedia.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService implements ImageStorageService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    /**
     * Inicializa la configuración de Cloudinary después de que se hayan inyectado las propiedades.
     */
    @PostConstruct
    public void init() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        this.cloudinary = new Cloudinary(config);
    }

    /**
     * Sube una imagen a Cloudinary y devuelve su URL pública.
     *
     * @param file Archivo de imagen a subir.
     * @return URL pública de la imagen subida.
     */
    public String uploadImage(MultipartFile file) {
        try {
            File uploadedFile = convertMultiPartToFile(file);
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(uploadedFile, ObjectUtils.emptyMap());
            
            boolean isDeleted = uploadedFile.delete();
            if (!isDeleted) {
                log.warn("No se pudo borrar el archivo temporal: {}", uploadedFile.getName());
            }

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            log.error("Error subiendo imagen a Cloudinary", e);
            throw new BadRequestException("Error al subir la imagen: " + e.getMessage());
        }
    }

    /**
     * Elimina una imagen de Cloudinary a partir de su URL pública.
     *
     * @param imageUrl URL pública de la imagen a eliminar.
     */
    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("No se pudo eliminar la imagen de Cloudinary: {}", e.getMessage());
        }
    }

    /**
     * Extrae el publicId de Cloudinary a partir de la URL pública de la imagen.
     *
     * @param url URL pública de Cloudinary.
     * @return publicId necesario para la API de Cloudinary.
     */
    private String extractPublicId(String url) {
        String[] parts = url.split("/upload/");
        if (parts.length < 2) return "";
        String afterUpload = parts[1];
        if (afterUpload.matches("v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }
        int dotIndex = afterUpload.lastIndexOf('.');
        if (dotIndex <= 0) return afterUpload;
        return afterUpload.substring(0, dotIndex);
    }

    /**
     * Convierte un archivo MultipartFile a un archivo File.
     *
     * @param file Archivo MultipartFile a convertir.
     * @return Archivo File convertido.
     * @throws IOException Si ocurre un error durante la conversión.
     */
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("upload");
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        File tempFile = Files.createTempFile("cloudinary_", "_" + safeName).toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        return tempFile;
    }
}