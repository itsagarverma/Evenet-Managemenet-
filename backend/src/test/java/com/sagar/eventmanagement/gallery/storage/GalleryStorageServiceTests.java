package com.sagar.eventmanagement.gallery.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GalleryStorageServiceTests {
    @TempDir Path temporaryDirectory;

    @Test
    void generatedObjectKeysUseNormalizedCategoryAndRandomSafeLeaf() {
        String first = GalleryObjectKeys.create("baby-shower", "PNG");
        String second = GalleryObjectKeys.create("engagement", "jpg");

        assertTrue(first.matches("baby-shower/[0-9a-f-]{36}\\.png"));
        assertTrue(second.matches("engagement/[0-9a-f-]{36}\\.jpg"));
        assertNotEquals(first, second);
        assertDoesNotThrow(() -> UUID.fromString(first.substring("baby-shower/".length(), first.length() - 4)));
        assertFalse(GalleryObjectKeys.isSafe("../secrets.jpg"));
        assertThrows(IllegalArgumentException.class, () -> GalleryObjectKeys.create("../barat", "jpg"));
    }

    @Test
    void localProviderStoresAndDeletesOnlySafeKeys() throws Exception {
        LocalGalleryStorageService local = new LocalGalleryStorageService(temporaryDirectory.toString());
        String key = GalleryObjectKeys.create("haldi", "png");
        byte[] image = new byte[]{1, 2, 3};

        local.put(key, "image/png", image);

        assertArrayEquals(image, Files.readAllBytes(local.resolve(key)));
        assertEquals("/api/gallery/media/" + key, local.publicUrl(key));
        local.delete(key);
        assertFalse(Files.exists(local.resolve(key)));
        assertThrows(GalleryStorageException.class, () -> local.delete("../../outside.png"));
    }

    @Test
    void supabaseProviderBuildsPublicUrlAndUsesConfiguredBucketForPutAndDelete() {
        S3Client s3 = mock(S3Client.class);
        SupabaseStorageProperties properties = new SupabaseStorageProperties();
        properties.setEndpoint("https://sample.storage.supabase.co/storage/v1/s3");
        properties.setRegion("sample-region");
        properties.setAccessKey("access-for-test");
        properties.setSecretKey("secret-for-test");
        properties.setBucket("gallery");
        properties.setPublicUrl("https://sample.supabase.co/storage/v1/object/public/");
        SupabaseGalleryStorageService service = new SupabaseGalleryStorageService(s3, properties);
        String key = "baby-shower/01234567-89ab-cdef-0123-456789abcdef.png";

        assertEquals("https://sample.supabase.co/storage/v1/object/public/gallery/" + key, service.publicUrl(key));
        assertTrue(service.servesPublicUrlsDirectly());
        service.put(key, "image/png", new byte[]{1, 2, 3});
        service.delete(key);

        var putCaptor = org.mockito.ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3).putObject(putCaptor.capture(), any(software.amazon.awssdk.core.sync.RequestBody.class));
        assertEquals("gallery", putCaptor.getValue().bucket());
        assertEquals(key, putCaptor.getValue().key());
        assertEquals("image/png", putCaptor.getValue().contentType());
        assertEquals("public, max-age=31536000, immutable", putCaptor.getValue().cacheControl());
        var deleteCaptor = org.mockito.ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3).deleteObject(deleteCaptor.capture());
        assertEquals("gallery", deleteCaptor.getValue().bucket());
        assertEquals(key, deleteCaptor.getValue().key());
        verifyNoMoreInteractions(s3);
    }

    @Test
    void supabaseConfigurationFailsClosedWhenRequiredValuesAreMissing() {
        SupabaseStorageProperties properties = new SupabaseStorageProperties();
        assertThrows(IllegalStateException.class, properties::validate);
    }
}
