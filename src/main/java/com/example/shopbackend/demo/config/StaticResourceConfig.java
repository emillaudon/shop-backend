package com.example.shopbackend.demo.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("dev")
public class StaticResourceConfig implements WebMvcConfigurer {

    private final String location;

    public StaticResourceConfig(@Value("${app.storage.local-root}") String localRoot) {
        Path root = Paths.get(localRoot).toAbsolutePath().normalize();

        this.location = root.toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
            .addResourceLocations(location);
    }
    
}
