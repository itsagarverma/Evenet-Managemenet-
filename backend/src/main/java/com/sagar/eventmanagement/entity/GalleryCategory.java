package com.sagar.eventmanagement.entity;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.time.Instant; import java.util.*;
@Entity @Table(name="gallery_categories") @Getter @Setter
public class GalleryCategory {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=100) private String name;
 @Column(nullable=false,unique=true,length=120) private String slug;
 @Column(length=1000) private String description;
 @Column(length=500) private String coverImage;
 @Column(nullable=false) private boolean published=false;
 @Column(nullable=false) private int displayOrder=0;
 @Column(nullable=false) private Instant createdAt=Instant.now();
 @Column(nullable=false) private Instant updatedAt=Instant.now();
 @OneToMany(mappedBy="category",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("displayOrder ASC,id ASC") private List<GalleryImage> images=new ArrayList<>();
 @PreUpdate void updateTimestamp(){updatedAt=Instant.now();}
}
