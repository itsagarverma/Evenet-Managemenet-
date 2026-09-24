package com.sagar.eventmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

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

    private Integer guestCount;
    @Column(length=120) private String budget;

    @Column(nullable=false) private Instant createdAt = Instant.now();
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=24) private LeadStatus status = LeadStatus.NEW;
    private LocalDate followUpDate;
    @Column(length=2000) private String followUpNote;
    @Column(length=500) private String nextAction;
}
