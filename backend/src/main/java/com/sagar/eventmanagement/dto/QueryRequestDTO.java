package com.sagar.eventmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryRequestDTO {

    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone cannot be empty")
    private String phone;

    private String eventType;

    private String eventDate;

    private String cityVenue;

    private String specialRequirements;

    private String message;
}
