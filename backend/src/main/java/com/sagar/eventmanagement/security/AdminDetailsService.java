package com.sagar.eventmanagement.security;
import com.sagar.eventmanagement.repository.AdminUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class AdminDetailsService implements UserDetailsService {
 private final AdminUserRepository users;
 public AdminDetailsService(AdminUserRepository users){this.users=users;}
 public UserDetails loadUserByUsername(String email){var user=users.findByEmailIgnoreCase(email).orElseThrow(()->new UsernameNotFoundException("Admin not found")); return new User(user.getEmail(),user.getPasswordHash(),List.of(()->"ROLE_ADMIN"));}
}
