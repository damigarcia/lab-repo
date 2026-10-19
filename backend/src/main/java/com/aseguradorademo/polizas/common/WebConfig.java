package com.aseguradorademo.polizas.common;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** Permite llamar a la API desde el servidor de desarrollo de Vite. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    /** Reloj inyectable para poder fijar "hoy" en los tests. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
