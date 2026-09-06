package com.sagar.eventmanagement.dto;

import lombok.Data;

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
}
