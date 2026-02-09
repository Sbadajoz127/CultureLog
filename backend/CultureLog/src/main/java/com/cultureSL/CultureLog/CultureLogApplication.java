package com.cultureSL.CultureLog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase de arranque de la aplicación Spring Boot.
 * <p>
 * Inicia la aplicación y configura los componentes necesarios para su funcionamiento.
 * </p>
 */
@SpringBootApplication
@EnableAsync
public class CultureLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(CultureLogApplication.class, args);
	}

}
