package com.cultureSL.CultureLog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración del pool de hilos utilizado por los métodos anotados con {@code @Async}.
 * <p>
 * Reemplaza el {@code SimpleAsyncTaskExecutor} por defecto (que crea hilos ilimitados)
 * con un pool acotado y reutilizable, evitando el agotamiento de recursos bajo carga.
 * </p>
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
