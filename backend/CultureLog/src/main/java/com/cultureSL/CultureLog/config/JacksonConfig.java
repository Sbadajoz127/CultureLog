package com.cultureSL.CultureLog.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.StringDeserializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Configuración global de Jackson para normalizar los strings entrantes.
 * <p>
 * Registra un deserializador personalizado que aplica {@link String#trim()}
 * a todos los campos de texto recibidos en los cuerpos JSON de las peticiones,
 * evitando que se persistan espacios en blanco innecesarios en la base de datos.
 * </p>
 */
@Configuration
public class JacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer stringTrimCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("StringTrimModule");
            module.addDeserializer(String.class, new TrimStringDeserializer());
            builder.addModule(module);
        };
    }

    private static class TrimStringDeserializer extends StringDeserializer {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String value = super.deserialize(p, ctxt);
            return value != null ? value.trim() : null;
        }
    }
}
