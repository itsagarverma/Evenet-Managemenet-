package com.sagar.eventmanagement.gallery.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@ConditionalOnProperty(prefix = "app.gallery.storage", name = "provider", havingValue = "supabase")
public class SupabaseGalleryStorageService implements GalleryStorageService {
    private static final Logger log = LoggerFactory.getLogger(SupabaseGalleryStorageService.class);
    private final S3Client s3;
    private final SupabaseStorageProperties properties;

    public SupabaseGalleryStorageService(S3Client s3, SupabaseStorageProperties properties) {
        properties.validate();
        this.s3 = s3;
        this.properties = properties;
    }

    @Override
    public void put(String objectKey, String contentType, byte[] content) {
        validateKey(objectKey);
        try {
            s3.putObject(PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .cacheControl("public, max-age=31536000, immutable")
                    .build(), RequestBody.fromBytes(content));
        } catch (AwsServiceException | SdkClientException ex) {
            log.warn("Supabase Storage upload failed for gallery object");
            throw new GalleryStorageException("Image upload failed in storage. Check the storage configuration and retry.");
        }
    }

    @Override
    public void delete(String objectKey) {
        validateKey(objectKey);
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(properties.getBucket()).key(objectKey).build());
        } catch (AwsServiceException | SdkClientException ex) {
            log.warn("Supabase Storage delete failed for gallery object");
            throw new GalleryStorageException("Image could not be deleted from storage. Retry the delete operation.");
        }
    }

    @Override
    public String publicUrl(String objectKey) {
        validateKey(objectKey);
        String encodedKey = java.util.Arrays.stream(objectKey.split("/"))
                .map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(java.util.stream.Collectors.joining("/"));
        return trimSlash(properties.getPublicUrl()) + "/" + properties.getBucket() + "/" + encodedKey;
    }

    @Override
    public boolean servesPublicUrlsDirectly() {
        return true;
    }

    private void validateKey(String key) {
        if (!GalleryObjectKeys.isSafe(key)) {
            throw new GalleryStorageException("Invalid image storage key");
        }
    }

    private String trimSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
