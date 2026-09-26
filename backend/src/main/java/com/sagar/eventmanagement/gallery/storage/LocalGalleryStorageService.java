package com.sagar.eventmanagement.gallery.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@ConditionalOnProperty(prefix = "app.gallery.storage", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalGalleryStorageService implements GalleryStorageService {
    private final Path root;

    public LocalGalleryStorageService(@Value("${app.upload.directory:./uploads}") String directory) {
        root = Path.of(directory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new IllegalStateException("Local gallery storage directory is unavailable");
        }
    }

    @Override
    public void put(String objectKey, String contentType, byte[] content) {
        Path target = resolve(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException ex) {
            throw new GalleryStorageException("Image could not be stored. Please retry.");
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
        } catch (IOException ex) {
            throw new GalleryStorageException("Image storage cleanup failed. Retry the delete operation.");
        }
    }

    public Path resolve(String objectKey) {
        if (!GalleryObjectKeys.isSafe(objectKey)) {
            throw new GalleryStorageException("Invalid image storage key");
        }
        Path target = root.resolve(objectKey).normalize();
        if (!target.startsWith(root)) {
            throw new GalleryStorageException("Invalid image storage key");
        }
        return target;
    }

    @Override
    public String publicUrl(String objectKey) {
        resolve(objectKey);
        return "/api/gallery/media/" + objectKey;
    }

    @Override
    public boolean servesPublicUrlsDirectly() {
        return false;
    }
}
