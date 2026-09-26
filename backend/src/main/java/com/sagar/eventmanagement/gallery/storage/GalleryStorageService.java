package com.sagar.eventmanagement.gallery.storage;

public interface GalleryStorageService {
    void put(String objectKey, String contentType, byte[] content);

    void delete(String objectKey);

    String publicUrl(String objectKey);

    boolean servesPublicUrlsDirectly();
}
