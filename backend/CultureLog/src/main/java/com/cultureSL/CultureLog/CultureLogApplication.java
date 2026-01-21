package com.cultureSL.CultureLog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CultureLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(CultureLogApplication.class, args);
	}

}
