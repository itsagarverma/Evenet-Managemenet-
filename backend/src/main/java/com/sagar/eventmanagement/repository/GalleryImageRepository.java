package com.sagar.eventmanagement.repository;
import com.sagar.eventmanagement.entity.GalleryImage; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface GalleryImageRepository extends JpaRepository<GalleryImage,Long>{List<GalleryImage> findByCategorySlugAndPublishedTrueOrderByDisplayOrderAscIdAsc(String slug); List<GalleryImage> findByCategoryIdOrderByDisplayOrderAscIdAsc(Long id); java.util.Optional<GalleryImage> findByStorageKey(String key);}
