package com.sagar.eventmanagement.repository;
import com.sagar.eventmanagement.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AdminUserRepository extends JpaRepository<AdminUser,Long> { Optional<AdminUser> findByEmailIgnoreCase(String email); }
