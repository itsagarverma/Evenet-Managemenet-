package com.sagar.eventmanagement.repository;
import com.sagar.eventmanagement.entity.ServiceItem; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface ServiceItemRepository extends JpaRepository<ServiceItem,Long>{List<ServiceItem> findByPublishedTrueOrderByDisplayOrderAsc();List<ServiceItem> findAllByOrderByDisplayOrderAsc();}
