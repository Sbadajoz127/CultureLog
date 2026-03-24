package com.cultureSL.CultureLog.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Contrato para el servicio de almacenamiento de imágenes.
 * <p>
 * Abstrae el proveedor concreto (Cloudinary, S3, etc.) permitiendo
 * cambiar la implementación sin afectar a los consumidores.
 * </p>
 */
public interface ImageStorageService {

    /**
     * Sube una imagen y devuelve su URL pública.
     *
     * @param file archivo de imagen a subir
     * @return URL pública de la imagen almacenada
     */
    String uploadImage(MultipartFile file);

    /**
     * Sube una imagen a partir de su URL remota y devuelve la URL pública almacenada.
     *
     * @param url URL remota de la imagen a subir
     * @return URL pública de la imagen almacenada
     */
    String uploadImageFromUrl(String url);

    /**
     * Elimina una imagen a partir de su URL pública.
     *
     * @param imageUrl URL pública de la imagen a eliminar
     */
    void deleteImage(String imageUrl);
}
