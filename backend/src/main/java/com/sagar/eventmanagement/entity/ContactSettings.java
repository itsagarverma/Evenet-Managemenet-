package com.sagar.eventmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "contact_settings")
@Getter
@Setter
public class ContactSettings {
    @Id
    private Long id = 1L;

    @Column(length = 254)
    private String email = "thesnehmoments@gmail.com";

    @Column(length = 40)
    private String phone = "+91 9302259211";

    @Column(length = 40)
    private String whatsapp = "+91 9302259211";

    @Column(name = "whatsapp_url", length = 500)
    private String whatsappUrl = "https://wa.me/919302259211";

    @Column(length = 500)
    private String instagram = "https://www.instagram.com/thesnehmoments";

    @Column(length = 500)
    private String facebook;

    @Column(length = 500)
    private String website = "https://thesnehmoments.in/";

    @Column(length = 500)
    private String address = "Bhopal, Madhya Pradesh";
}
