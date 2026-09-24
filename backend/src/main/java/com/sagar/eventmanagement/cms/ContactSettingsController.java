package com.sagar.eventmanagement.cms;

import com.sagar.eventmanagement.entity.ContactSettings;
import com.sagar.eventmanagement.repository.ContactSettingsRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ContactSettingsController {
    private final ContactSettingsRepository repository;

    public ContactSettingsController(ContactSettingsRepository repository) { this.repository = repository; }

    public record ContactView(String email, String phone, String whatsapp, String whatsappUrl,
                              String instagram, String facebook, String website, String address) {}

    public record ContactInput(@Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") @Size(max = 254) String email,
                               @Pattern(regexp = "^$|\\+?[0-9 ()-]{7,24}") @Size(max = 40) String phone,
                               @NotBlank @Pattern(regexp = "\\+?[0-9 ()-]{7,24}") @Size(max = 40) String whatsapp,
                               @NotBlank @Pattern(regexp = "https://wa\\.me/[0-9]{7,15}", message = "Use an https://wa.me/number link") String whatsappUrl,
                               @Pattern(regexp = "^$|https://(www\\.)?instagram\\.com/[^\\s]+", message = "Enter an Instagram https URL") @Size(max = 500) String instagram,
                               @Pattern(regexp = "^$|https://[^\\s]+", message = "Enter an https URL") @Size(max = 500) String facebook,
                               @Pattern(regexp = "^$|https://[^\\s]+", message = "Enter an https URL") @Size(max = 500) String website,
                               @Size(max = 500) String address) {}

    @GetMapping("/api/contact-settings")
    public ContactView publicSettings() { return view(current()); }

    @GetMapping("/api/admin/contact-settings")
    public ContactView adminSettings() { return view(current()); }

    @PutMapping("/api/admin/contact-settings")
    public ContactView update(@Valid @RequestBody ContactInput input) {
        if (input.whatsapp() != null && !input.whatsapp().isBlank() && input.whatsappUrl() != null && !input.whatsappUrl().isBlank()) {
            String phoneDigits = input.whatsapp().replaceAll("\\D", "");
            String linkDigits = input.whatsappUrl().replaceAll("\\D", "");
            if (!phoneDigits.equals(linkDigits)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WhatsApp number and URL must match");
            }
        }
        ContactSettings settings = current();
        settings.setEmail(trim(input.email()));
        settings.setPhone(trim(input.phone()));
        settings.setWhatsapp(trim(input.whatsapp()));
        settings.setWhatsappUrl(trim(input.whatsappUrl()));
        settings.setInstagram(trim(input.instagram()));
        settings.setFacebook(trim(input.facebook()));
        settings.setWebsite(trim(input.website()));
        settings.setAddress(trim(input.address()));
        return view(repository.save(settings));
    }

    private ContactSettings current() {
        return repository.findById(1L).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private static String trim(String value) { return value == null ? null : value.trim(); }
    private static ContactView view(ContactSettings x) {
        return new ContactView(x.getEmail(), x.getPhone(), x.getWhatsapp(), x.getWhatsappUrl(),
                x.getInstagram(), x.getFacebook(), x.getWebsite(), x.getAddress());
    }
}
