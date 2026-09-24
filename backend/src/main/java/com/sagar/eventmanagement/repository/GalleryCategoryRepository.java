package com.sagar.eventmanagement.repository;
import com.sagar.eventmanagement.entity.GalleryCategory; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface GalleryCategoryRepository extends JpaRepository<GalleryCategory,Long>{Optional<GalleryCategory> findBySlug(String slug); boolean existsBySlug(String slug); List<GalleryCategory> findByPublishedTrueOrderByDisplayOrderAsc(); List<GalleryCategory> findAllByOrderByDisplayOrderAsc();}
