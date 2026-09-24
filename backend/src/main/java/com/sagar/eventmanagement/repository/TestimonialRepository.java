package com.sagar.eventmanagement.repository;
import com.sagar.eventmanagement.entity.Testimonial; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface TestimonialRepository extends JpaRepository<Testimonial,Long>{List<Testimonial> findByPublishedTrueOrderByDisplayOrderAsc();List<Testimonial> findAllByOrderByDisplayOrderAsc();}
