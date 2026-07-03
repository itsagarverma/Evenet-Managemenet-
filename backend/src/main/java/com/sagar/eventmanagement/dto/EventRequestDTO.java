package com.sagar.eventmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventRequestDTO {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotBlank(message = "Location cannot be empty")
    private String location;

    private String eventDate;
}
