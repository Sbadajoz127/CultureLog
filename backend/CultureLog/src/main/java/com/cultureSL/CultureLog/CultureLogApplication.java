package com.cultureSL.CultureLog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase de arranque de la aplicación CultureLog (Spring Boot).
 * <p>
 * CultureLog es una red social de cultura donde los usuarios pueden registrar,
 * valorar y compartir obras multimedia (películas, libros, series, videojuegos, etc.)
 * con su comunidad de seguidores.
 * </p>
 * <p>
 * La anotación {@code @EnableAsync} habilita la ejecución asíncrona de métodos
 * (utilizada en el envío de emails y creación de notificaciones).
 * </p>
 */
@SpringBootApplication
@EnableAsync
public class CultureLogApplication {

	private static final Logger log = Logger.getLogger(CultureLogApplication.class.getName());

	/**
	 * Punto de entrada principal de la aplicación.
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		loadEnvFile();
		SpringApplication.run(CultureLogApplication.class, args);
	}

	/**
	 * Carga las variables definidas en el archivo {@code .env} como
	 * propiedades del sistema para que Spring pueda resolverlas
	 * mediante placeholders {@code ${...}} en application.properties.
	 */
	private static void loadEnvFile() {
		Path envPath = Path.of(".env");
		if (!Files.exists(envPath)) {
			return;
		}
		try {
			Files.readAllLines(envPath).forEach(line -> {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					return;
				}
				int idx = line.indexOf('=');
				if (idx > 0) {
					String key = line.substring(0, idx).trim();
					String value = line.substring(idx + 1).trim();
					if ((value.startsWith("\"") && value.endsWith("\"")) ||
						(value.startsWith("'") && value.endsWith("'"))) {
						value = value.substring(1, value.length() - 1);
					}
					if (System.getProperty(key) == null && System.getenv(key) == null) {
						System.setProperty(key, value);
					}
				}
			});
		} catch (IOException e) {
			log.log(Level.WARNING, "No se pudo leer el archivo .env: {0}", e.getMessage());
		}
	}

}
