package com.sagar.eventmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Query {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
