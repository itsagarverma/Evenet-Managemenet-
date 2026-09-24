package com.sagar.eventmanagement.cms;
import com.sagar.eventmanagement.entity.*; import com.sagar.eventmanagement.repository.*; import jakarta.validation.Valid; import jakarta.validation.constraints.NotBlank; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException; import org.springframework.http.HttpStatus; import java.util.List;
@RestController public class CmsController {
 private final ServiceItemRepository services; private final TestimonialRepository testimonials;
 public CmsController(ServiceItemRepository s,TestimonialRepository t){services=s;testimonials=t;}
 public record ServiceView(Long id,String name,String description,String imageUrl,boolean published,int displayOrder){}
 public record TestimonialView(Long id,String name,String eventType,String location,String review,boolean published,int displayOrder){}
 public record ServiceInput(@NotBlank String name,String description,String imageUrl,boolean published,int displayOrder){}
 public record TestimonialInput(@NotBlank String name,String eventType,String location,@NotBlank String review,boolean published,int displayOrder){}
 @GetMapping("/api/services") public List<ServiceView> services(){return services.findByPublishedTrueOrderByDisplayOrderAsc().stream().map(CmsController::serviceView).toList();}
 @GetMapping("/api/testimonials") public List<TestimonialView> testimonials(){return testimonials.findByPublishedTrueOrderByDisplayOrderAsc().stream().map(CmsController::testimonialView).toList();}
 @GetMapping("/api/admin/services") public List<ServiceView> adminServices(){return services.findAllByOrderByDisplayOrderAsc().stream().map(CmsController::serviceView).toList();}
 @PostMapping("/api/admin/services") public ServiceView addService(@Valid @RequestBody ServiceInput i){var x=new ServiceItem();set(x,i);return serviceView(services.save(x));}
 @PutMapping("/api/admin/services/{id}") public ServiceView updateService(@PathVariable Long id,@Valid @RequestBody ServiceInput i){var x=services.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));set(x,i);return serviceView(services.save(x));}
 @DeleteMapping("/api/admin/services/{id}") public void deleteService(@PathVariable Long id){services.deleteById(id);}
 private void set(ServiceItem x,ServiceInput i){x.setName(i.name());x.setDescription(i.description());x.setImageUrl(i.imageUrl());x.setPublished(i.published());x.setDisplayOrder(i.displayOrder());}
 @GetMapping("/api/admin/testimonials") public List<TestimonialView> adminTestimonials(){return testimonials.findAllByOrderByDisplayOrderAsc().stream().map(CmsController::testimonialView).toList();}
 @PostMapping("/api/admin/testimonials") public TestimonialView addTestimonial(@Valid @RequestBody TestimonialInput i){var x=new Testimonial();set(x,i);return testimonialView(testimonials.save(x));}
 @PutMapping("/api/admin/testimonials/{id}") public TestimonialView updateTestimonial(@PathVariable Long id,@Valid @RequestBody TestimonialInput i){var x=testimonials.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));set(x,i);return testimonialView(testimonials.save(x));}
 @DeleteMapping("/api/admin/testimonials/{id}") public void deleteTestimonial(@PathVariable Long id){testimonials.deleteById(id);}
 private void set(Testimonial x,TestimonialInput i){x.setName(i.name());x.setEventType(i.eventType());x.setLocation(i.location());x.setReview(i.review());x.setPublished(i.published());x.setDisplayOrder(i.displayOrder());}
 private static ServiceView serviceView(ServiceItem x){return new ServiceView(x.getId(),x.getName(),x.getDescription(),x.getImageUrl(),x.isPublished(),x.getDisplayOrder());}
 private static TestimonialView testimonialView(Testimonial x){return new TestimonialView(x.getId(),x.getName(),x.getEventType(),x.getLocation(),x.getReview(),x.isPublished(),x.getDisplayOrder());}
}
