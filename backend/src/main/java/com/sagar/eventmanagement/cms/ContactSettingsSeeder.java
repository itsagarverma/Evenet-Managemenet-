package com.sagar.eventmanagement.cms;

import com.sagar.eventmanagement.entity.ContactSettings;
import com.sagar.eventmanagement.repository.ContactSettingsRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContactSettingsSeeder {
    @Bean
    ApplicationRunner seedContactSettings(ContactSettingsRepository repository) {
        return args -> {
            if (!repository.existsById(1L)) repository.save(new ContactSettings());
        };
    }
}
