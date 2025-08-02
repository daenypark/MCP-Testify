package com.wedding.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
@Table(name = "wedding_events")
public class WeddingEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Event name is required")
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Event date is required")
    @Column(name = "event_date", nullable = false)
    @JsonProperty("eventDate")
    private LocalDateTime eventDate;
    
    @Column(name = "venue_name", length = 200)
    @JsonProperty("venueName")
    private String venueName;
    
    @Column(name = "venue_address", columnDefinition = "TEXT")
    @JsonProperty("venueAddress")
    private String venueAddress;
    
    @Column(name = "dress_code", length = 100)
    @JsonProperty("dressCode")
    private String dressCode;
    
    @Column(name = "created_at")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    // Constructors
    public WeddingEvent() {
        this.createdAt = LocalDateTime.now();
    }
    
    public WeddingEvent(String name, LocalDateTime eventDate) {
        this();
        this.name = name;
        this.eventDate = eventDate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getEventDate() {
        return eventDate;
    }
    
    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }
    
    public String getVenueName() {
        return venueName;
    }
    
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
    
    public String getVenueAddress() {
        return venueAddress;
    }
    
    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }
    
    public String getDressCode() {
        return dressCode;
    }
    
    public void setDressCode(String dressCode) {
        this.dressCode = dressCode;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "WeddingEvent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", eventDate=" + eventDate +
                ", venueName='" + venueName + '\'' +
                '}';
    }
} 