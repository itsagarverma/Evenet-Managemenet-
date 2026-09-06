package com.sagar.eventmanagement.dto;

import lombok.Data;

@Data
public class EventResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String location;
    private String eventDate;
}
