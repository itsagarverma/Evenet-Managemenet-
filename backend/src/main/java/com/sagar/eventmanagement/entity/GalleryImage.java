package com.sagar.eventmanagement.entity;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.time.Instant;
@Entity @Table(name="gallery_images") @Getter @Setter
public class GalleryImage {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="category_id",nullable=false) private GalleryCategory category;
 @Column(nullable=false,length=300) private String storageKey;
 @Column(length=300) private String altText;
 @Column(nullable=false) private int displayOrder=0;
 @Column(nullable=false) private boolean published=false;
 @Column(nullable=false) private Instant createdAt=Instant.now();
}
