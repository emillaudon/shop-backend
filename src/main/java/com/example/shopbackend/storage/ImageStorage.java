package com.example.shopbackend.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

    String save(MultipartFile file, String key);

    void delete(String key);
}

