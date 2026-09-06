package com.sagar.eventmanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends query notification emails via Brevo's HTTPS API (not raw SMTP).
 * This is necessary because Render's free tier blocks outbound SMTP ports (25, 465, 587),
 * but regular HTTPS (port 443) works fine - which is exactly what this API uses.
 */
@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    private final RestClient restClient = RestClient.create("https://api.brevo.com/v3");

    public void sendQueryNotification(String to, String subject, String body) {
        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", "The Sneh Moments", "email", senderEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", body
        );

        restClient.post()
                .uri("/smtp/email")
                .header("api-key", brevoApiKey)
                .header("Content-Type", "application/json")
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
