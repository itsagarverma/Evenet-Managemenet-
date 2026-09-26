package com.sagar.eventmanagement.dto;

import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import com.sagar.eventmanagement.entity.LeadStatus;

@Data
public class QueryResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String eventType;
    private String eventDate;
    private String cityVenue;
    private String specialRequirements;
    private String message;
    private Integer guestCount;
    private Double budget;
    private Instant createdAt;
    private LeadStatus status;
    private LocalDate followUpDate;
    private String followUpNote;
    private String nextAction;
}
