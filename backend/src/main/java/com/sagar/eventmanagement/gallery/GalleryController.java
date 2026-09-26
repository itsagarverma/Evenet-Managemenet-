package com.sagar.eventmanagement.gallery;

import com.sagar.eventmanagement.entity.GalleryCategory;
import com.sagar.eventmanagement.entity.GalleryImage;
import com.sagar.eventmanagement.gallery.storage.GalleryObjectKeys;
import com.sagar.eventmanagement.gallery.storage.GalleryStorageException;
import com.sagar.eventmanagement.gallery.storage.GalleryStorageService;
import com.sagar.eventmanagement.gallery.storage.LocalGalleryStorageService;
import com.sagar.eventmanagement.repository.GalleryCategoryRepository;
import com.sagar.eventmanagement.repository.GalleryImageRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {
    private static final Logger log = LoggerFactory.getLogger(GalleryController.class);
    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;
    private static final int MAX_IMAGES_PER_REQUEST = 20;

    private final GalleryCategoryRepository categories;
    private final GalleryImageRepository images;
    private final GalleryStorageService storage;
    private final ObjectProvider<LocalGalleryStorageService> localStorage;

    public GalleryController(GalleryCategoryRepository categories,
                             GalleryImageRepository images,
                             GalleryStorageService storage,
                             ObjectProvider<LocalGalleryStorageService> localStorage) {
        this.categories = categories;
        this.images = images;
        this.storage = storage;
        this.localStorage = localStorage;
    }

    public record CategoryInput(@NotBlank String name, @NotBlank String slug, String description,
                                String coverImage, boolean published, int displayOrder) {}
    public record ImageView(Long id, String url, String altText, int displayOrder, boolean published) {}
    public record CategoryView(Long id, String name, String slug, String description, String coverImage,
                               boolean published, int displayOrder, List<ImageView> images) {}
    public record UploadItemResult(String fileName, boolean success, ImageView image, String error) {}
    public record UploadBatchResult(List<UploadItemResult> items) {}

    @GetMapping("/categories")
    public List<CategoryView> publicCategories() {
        return categories.findByPublishedTrueOrderByDisplayOrderAsc().stream().map(c -> view(c, true)).toList();
    }

    @GetMapping("/categories/{slug}")
    public CategoryView publicCategory(@PathVariable String slug) {
        GalleryCategory category = categories.findBySlug(slug).filter(GalleryCategory::isPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return view(category, true);
    }

    @GetMapping("/categories/{slug}/images")
    public List<ImageView> publicImages(@PathVariable String slug) {
        return publicCategory(slug).images();
    }

    @GetMapping("/admin/categories")
    public List<CategoryView> adminCategories() {
        return categories.findAllByOrderByDisplayOrderAsc().stream().map(c -> view(c, false)).toList();
    }

    @PostMapping("/admin/categories")
    public CategoryView create(@Valid @RequestBody CategoryInput input) {
        String slug = normalizeSlug(input.slug());
        validateSlug(slug);
        if (categories.existsBySlug(slug)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
        GalleryCategory category = new GalleryCategory();
        apply(category, input, slug);
        return view(categories.save(category), false);
    }

    @PutMapping("/admin/categories/{id}")
    public CategoryView update(@PathVariable Long id, @Valid @RequestBody CategoryInput input) {
        GalleryCategory category = categories.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String slug = normalizeSlug(input.slug());
        validateSlug(slug);
        if (categories.findBySlug(slug).filter(other -> !other.getId().equals(id)).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
        }
        apply(category, input, slug);
        return view(categories.save(category), false);
    }

    @DeleteMapping("/admin/categories/{id}")
    public void deleteCategory(@PathVariable Long id) {
        GalleryCategory category = categories.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        for (GalleryImage image : new ArrayList<>(images.findByCategoryIdOrderByDisplayOrderAscIdAsc(id))) {
            deleteStoredImage(image);
        }
        categories.delete(category);
    }

    @PostMapping(value = "/admin/categories/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadBatchResult upload(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
        GalleryCategory category = categories.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (files.isEmpty() || files.size() > MAX_IMAGES_PER_REQUEST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload between 1 and 20 images");
        }
        List<UploadItemResult> results = new ArrayList<>();
        int order = images.findByCategoryIdOrderByDisplayOrderAscIdAsc(id).size();
        for (MultipartFile file : files) {
            String fileName = safeFileName(file.getOriginalFilename());
            try {
                String extension = validateImage(file);
                byte[] content = file.getBytes();
                String key = GalleryObjectKeys.create(category.getSlug(), extension);
                storage.put(key, "image/" + (extension.equals("jpg") ? "jpeg" : extension), content);

                GalleryImage image = new GalleryImage();
                image.setCategory(category);
                image.setStorageKey(key);
                image.setPublished(false);
                image.setDisplayOrder(order++);
                try {
                    image = images.saveAndFlush(image);
                } catch (RuntimeException databaseFailure) {
                    try {
                        storage.delete(key);
                    } catch (GalleryStorageException cleanupFailure) {
                        log.warn("Gallery object cleanup failed after metadata persistence error for key {}", key);
                        throw new GalleryStorageException("Image metadata could not be saved and object cleanup failed; retry cleanup for " + key);
                    }
                    throw new GalleryStorageException("Image metadata could not be saved; the uploaded object was cleaned up.");
                }
                results.add(new UploadItemResult(fileName, true, imageView(image), null));
            } catch (ResponseStatusException failure) {
                results.add(new UploadItemResult(fileName, false, null, failure.getReason() == null ? failure.getMessage() : failure.getReason()));
            } catch (GalleryStorageException failure) {
                results.add(new UploadItemResult(fileName, false, null, failure.getMessage()));
            } catch (Exception failure) {
                results.add(new UploadItemResult(fileName, false, null, "Upload failed. Check the file and storage configuration, then retry."));
            }
        }
        return new UploadBatchResult(results);
    }

    @PutMapping("/admin/images/{id}")
    public ImageView editImage(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        GalleryImage image = images.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (body.containsKey("altText")) image.setAltText(Objects.toString(body.get("altText"), ""));
        if (body.containsKey("published")) image.setPublished(Boolean.parseBoolean(body.get("published").toString()));
        if (body.containsKey("displayOrder")) image.setDisplayOrder(Integer.parseInt(body.get("displayOrder").toString()));
        return imageView(images.save(image));
    }

    @DeleteMapping("/admin/images/{id}")
    public void deleteImage(@PathVariable Long id) {
        GalleryImage image = images.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        deleteStoredImage(image);
    }

    @GetMapping("/media/{*key}")
    public ResponseEntity<?> media(@PathVariable String key) {
        String objectKey = key.startsWith("/") ? key.substring(1) : key;
        GalleryImage image = images.findByStorageKey(objectKey)
                .filter(GalleryImage::isPublished)
                .filter(x -> x.getCategory().isPublished())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (storage.servesPublicUrlsDirectly()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(storage.publicUrl(image.getStorageKey()))).build();
        }
        LocalGalleryStorageService local = localStorage.getIfAvailable();
        if (local == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var path = local.resolve(image.getStorageKey());
        if (!java.nio.file.Files.isRegularFile(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(path.getFileName().toString()).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(resource);
    }

    private void deleteStoredImage(GalleryImage image) {
        GalleryCategory category = image.getCategory();
        String imageUrl = storage.publicUrl(image.getStorageKey());
        String legacyApiUrl = "/api/gallery/media/" + image.getStorageKey();
        String coverImage = category.getCoverImage();
        if (Objects.equals(coverImage, imageUrl) || Objects.equals(coverImage, legacyApiUrl)
                || (coverImage != null && coverImage.endsWith(legacyApiUrl))) {
            category.setCoverImage(null);
            categories.save(category);
        }
        image.setPublished(false);
        images.save(image);
        storage.delete(image.getStorageKey());
        images.delete(image);
    }

    private String validateImage(MultipartFile file) {
        if (file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty image file");
        if (file.getSize() > MAX_FILE_BYTES) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Each image must be at most 10 MB");
        String declared = file.getContentType();
        if (!List.of("image/jpeg", "image/png", "image/gif").contains(declared)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPEG, PNG, and GIF images are allowed");
        }
        String format;
        try (ImageInputStream stream = ImageIO.createImageInputStream(file.getInputStream())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                format = reader.getFormatName().toLowerCase(Locale.ROOT);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 8000 || height > 8000 || (long) width * height > 30_000_000L) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image dimensions exceed the 30-megapixel limit");
                }
                if (reader.read(0) == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
            } finally {
                reader.dispose();
            }
        } catch (ResponseStatusException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
        }
        String expected = switch (format) {
            case "jpeg", "jpg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> "";
        };
        if (!expected.equals(declared)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image content does not match its declared type");
        return format.equals("jpeg") ? "jpg" : format;
    }

    private CategoryView view(GalleryCategory category, boolean publicOnly) {
        List<ImageView> result = (publicOnly
                ? images.findByCategorySlugAndPublishedTrueOrderByDisplayOrderAscIdAsc(category.getSlug())
                : images.findByCategoryIdOrderByDisplayOrderAscIdAsc(category.getId()))
                .stream()
                .filter(image -> !publicOnly || image.isPublished())
                .map(this::imageView)
                .toList();
        return new CategoryView(category.getId(), category.getName(), category.getSlug(), category.getDescription(),
                category.getCoverImage(), category.isPublished(), category.getDisplayOrder(), result);
    }

    private ImageView imageView(GalleryImage image) {
        return new ImageView(image.getId(), storage.publicUrl(image.getStorageKey()), image.getAltText(),
                image.getDisplayOrder(), image.isPublished());
    }

    private void apply(GalleryCategory category, CategoryInput input, String slug) {
        category.setName(input.name().trim());
        category.setSlug(slug);
        category.setDescription(input.description());
        category.setCoverImage(input.coverImage());
        category.setPublished(input.published());
        category.setDisplayOrder(input.displayOrder());
    }

    private String normalizeSlug(String slug) { return slug == null ? "" : slug.trim().toLowerCase(Locale.ROOT); }

    private void validateSlug(String slug) {
        if (!slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug must contain lowercase letters, numbers, and single hyphens");
        }
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return "(unnamed image)";
        String name = fileName.replace('\\', '/');
        return name.substring(name.lastIndexOf('/') + 1);
    }
}
