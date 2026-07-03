package com.sagar.eventmanagement.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "events")
@Data
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be empty")
private String title;

@NotBlank(message = "Description cannot be empty")
private String description;

@NotBlank(message = "Location cannot be empty")
private String location;


    private String eventDate;
}
