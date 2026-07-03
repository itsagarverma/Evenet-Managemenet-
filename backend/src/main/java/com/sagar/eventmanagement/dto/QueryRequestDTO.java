package com.sagar.eventmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QueryRequestDTO {

    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone cannot be empty")
    private String phone;

    @NotBlank(message = "Event type cannot be empty")
    private String eventType;

    private String eventDate;

    @NotBlank(message = "City/Venue cannot be empty")
    private String cityVenue;

    @NotNull(message = "Budget cannot be empty")
    private Double budget;

    @NotNull(message = "Number of guests cannot be empty")
    private Integer numberOfGuests;

    private String specialRequirements;

    private String message;
}
