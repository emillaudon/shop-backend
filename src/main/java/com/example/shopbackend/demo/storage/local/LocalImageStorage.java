package com.example.shopbackend.demo.storage.local;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.shopbackend.demo.storage.ImageStorage;

@Service
@Profile("dev")
public class LocalImageStorage implements ImageStorage {

    private final Path root;

    public LocalImageStorage(@Value("${app.storage.local-root}") String localRoot) {
        this.root = Paths.get(localRoot).toAbsolutePath().normalize();
    }

    @Override
    public String save(MultipartFile file, String key) {
        Path target = root.resolve(key).normalize();

        if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid storage key (path traversal detected): " + key);
        
        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image to local storage: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        Path target = root.resolve(key).normalize();

        if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid storage key (path traversal detected): " + key);

        try {
            Files.deleteIfExists(target);
        }catch (IOException e) {
            throw new RuntimeException("Failed to delete image from local storage: " + key, e);
        }
    }

    
} 