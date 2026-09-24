package com.sagar.eventmanagement.gallery;
import com.sagar.eventmanagement.entity.GalleryCategory; import com.sagar.eventmanagement.repository.GalleryCategoryRepository; import org.springframework.boot.ApplicationArguments; import org.springframework.boot.ApplicationRunner; import org.springframework.stereotype.Component; import java.util.*;
@Component public class GallerySeeder implements ApplicationRunner {
 private final GalleryCategoryRepository repo; public GallerySeeder(GalleryCategoryRepository repo){this.repo=repo;}
 private record InitialCategory(String name,String slug,String cover){}
 public void run(ApplicationArguments args){
  List<InitialCategory> initial=List.of(
   new InitialCategory("Barat","barat","assets/images/hero-baraat.jpeg"),
   new InitialCategory("Haldi","haldi","https://images.unsplash.com/photo-1647949940712-bfcf82015d9b?w=800&h=1000&fit=crop&auto=format"),
   new InitialCategory("Mehendi","mehendi","https://images.unsplash.com/photo-1686865604150-43f95d61416c?w=800&h=1000&fit=crop&auto=format"),
   new InitialCategory("Mandap","mandap","assets/images/tailored-gallery-1.jpeg"),
   new InitialCategory("Sangeet","sangeet","https://images.unsplash.com/photo-1640745676611-bee05627a23c?w=800&h=1000&fit=crop&auto=format"),
   new InitialCategory("Birthday","birthday","https://images.unsplash.com/photo-1729237261091-bae8eba0c60c?w=800&h=1000&fit=crop&auto=format"),
   new InitialCategory("Baby Shower","baby-shower","assets/images/tailored-gallery-2.jpeg"),
   new InitialCategory("Show Flow","show-flow","assets/images/tailored-gallery-2.jpeg"),
   new InitialCategory("Reception","reception","assets/images/tailored-gallery-3.jpeg")
  );
  int order=0;for(var item:initial){if(!repo.existsBySlug(item.slug())){var c=new GalleryCategory();c.setName(item.name());c.setSlug(item.slug());c.setCoverImage(item.cover());c.setPublished(true);c.setDisplayOrder(order);repo.save(c);}order++;}
 }
}
