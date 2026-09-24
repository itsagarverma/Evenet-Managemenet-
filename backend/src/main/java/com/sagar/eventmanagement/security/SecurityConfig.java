package com.sagar.eventmanagement.security;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.beans.factory.annotation.Value;
@Configuration
public class SecurityConfig {
 @Value("${app.cookies.secure:false}") private boolean secureCookies;
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c)throws Exception{return c.getAuthenticationManager();}
 @Bean SecurityFilterChain filterChain(HttpSecurity http)throws Exception{
  var csrfRepo=CookieCsrfTokenRepository.withHttpOnlyFalse(); csrfRepo.setCookiePath("/"); csrfRepo.setCookieCustomizer(cookie->cookie.sameSite(secureCookies?"None":"Lax").secure(secureCookies));
  http.csrf(c->c.csrfTokenRepository(csrfRepo).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
   .cors(c->{})
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
   .authorizeHttpRequests(a->a.requestMatchers("/api/auth/login","/api/auth/csrf").permitAll()
    .requestMatchers(HttpMethod.GET,"/api/gallery/categories","/api/gallery/categories/*","/api/gallery/categories/*/images","/api/gallery/media/**","/api/testimonials","/api/services","/api/contact-settings").permitAll()
    .requestMatchers(HttpMethod.POST,"/queries").permitAll()
    .requestMatchers(HttpMethod.GET,"/events","/events/**").permitAll()
    .requestMatchers("/api/auth/logout").authenticated()
    .requestMatchers("/api/**","/queries/**","/events").hasRole("ADMIN")
    .requestMatchers("/events/**").hasRole("ADMIN")
    .anyRequest().permitAll())
   .formLogin(f->f.disable()).httpBasic(h->h.disable());
  return http.build();
 }
}
