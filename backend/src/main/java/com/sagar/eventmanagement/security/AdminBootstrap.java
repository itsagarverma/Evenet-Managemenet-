package com.sagar.eventmanagement.security;
import com.sagar.eventmanagement.entity.AdminUser;
import com.sagar.eventmanagement.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class AdminBootstrap implements ApplicationRunner {
 private final AdminUserRepository users; private final PasswordEncoder encoder;
 @Value("${ADMIN_BOOTSTRAP_EMAIL:}") private String email;
 @Value("${ADMIN_BOOTSTRAP_PASSWORD:}") private String password;
 public AdminBootstrap(AdminUserRepository users,PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
 public void run(ApplicationArguments args){if(users.count()>0)return;if(email.isBlank()&&password.isBlank())return;if(email.isBlank()||password.isBlank()||password.length()<12)throw new IllegalStateException("Set ADMIN_BOOTSTRAP_EMAIL and a password of at least 12 characters together");var u=new AdminUser();u.setEmail(email.trim().toLowerCase());u.setPasswordHash(encoder.encode(password));users.save(u);}
}
