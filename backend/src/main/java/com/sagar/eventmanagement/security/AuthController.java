package com.sagar.eventmanagement.security;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.*;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.sagar.eventmanagement.repository.AdminUserRepository;
import java.util.Map;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AuthenticationManager auth; private final AdminUserRepository users; private final PasswordEncoder encoder;
 public AuthController(AuthenticationManager auth,AdminUserRepository users,PasswordEncoder encoder){this.auth=auth;this.users=users;this.encoder=encoder;}
 @GetMapping("/csrf") public Map<String,String> csrf(@RequestAttribute(name="_csrf") org.springframework.security.web.csrf.CsrfToken token){return Map.of("token",token.getToken());}
 public record Login(@Email @NotBlank String email,@NotBlank String password){}
 @PostMapping("/login") public Map<String,String> login(@Valid @RequestBody Login in,HttpServletRequest req){Authentication a;try{a=auth.authenticate(new UsernamePasswordAuthenticationToken(in.email(),in.password()));}catch(AuthenticationException e){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");}req.getSession(true);req.changeSessionId();var context=SecurityContextHolder.createEmptyContext();context.setAuthentication(a);SecurityContextHolder.setContext(context);req.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,context);return Map.of("email",a.getName());}
 @PostMapping("/logout") public void logout(HttpServletRequest req){var s=req.getSession(false);if(s!=null)s.invalidate();SecurityContextHolder.clearContext();}
 @GetMapping("/me") public Map<String,String> me(Authentication auth){return Map.of("email",auth.getName());}
 public record PasswordChange(@NotBlank String currentPassword,@NotBlank @Size(min=12) String newPassword){}
 @PostMapping("/password") public void change(@Valid @RequestBody PasswordChange body,Authentication auth){var user=users.findByEmailIgnoreCase(auth.getName()).orElseThrow();if(!encoder.matches(body.currentPassword(),user.getPasswordHash()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Current password is incorrect");user.setPasswordHash(encoder.encode(body.newPassword()));users.save(user);}
}
