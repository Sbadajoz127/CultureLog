package com.cultureSL.CultureLog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

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

	/**
	 * Punto de entrada principal de la aplicación.
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(CultureLogApplication.class, args);
	}

}
