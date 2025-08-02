package com.wedding.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
@Table(name = "rsvps")
public class RSVP {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Guest ID is required")
    @Column(name = "guest_id", nullable = false)
    @JsonProperty("guestId")
    private Long guestId;
    
    @NotNull(message = "RSVP status is required")
    @Convert(converter = RSVPStatusConverter.class)
    @Column(nullable = false, length = 20)
    private RSVPStatus status;
    
    @Column(name = "plus_one_attending")
    @JsonProperty("plusOneAttending")
    private Boolean plusOneAttending = false;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "submitted_at")
    @JsonProperty("submittedAt")
    private LocalDateTime submittedAt;
    
    // Constructors
    public RSVP() {
        this.submittedAt = LocalDateTime.now();
    }
    
    public RSVP(Long guestId, RSVPStatus status) {
        this();
        this.guestId = guestId;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getGuestId() {
        return guestId;
    }
    
    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }
    
    public RSVPStatus getStatus() {
        return status;
    }
    
    public void setStatus(RSVPStatus status) {
        this.status = status;
    }
    
    public Boolean getPlusOneAttending() {
        return plusOneAttending;
    }
    
    public void setPlusOneAttending(Boolean plusOneAttending) {
        this.plusOneAttending = plusOneAttending;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    // Enum for RSVP status
    public enum RSVPStatus {
        ATTENDING("attending"),
        NOT_ATTENDING("not_attending"), 
        MAYBE("maybe");
        
        private final String value;
        
        RSVPStatus(String value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }
        
        @com.fasterxml.jackson.annotation.JsonCreator
        public static RSVPStatus fromString(String value) {
            if (value == null) return null;
            for (RSVPStatus status : RSVPStatus.values()) {
                if (status.value.equalsIgnoreCase(value) || 
                    status.name().equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid RSVP status: " + value + 
                ". Valid values are: attending, not_attending, maybe");
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    // JPA AttributeConverter to handle enum persistence
    @Converter(autoApply = true)
    public static class RSVPStatusConverter implements AttributeConverter<RSVPStatus, String> {
        
        @Override
        public String convertToDatabaseColumn(RSVPStatus status) {
            if (status == null) {
                return null;
            }
            return status.getValue();
        }
        
        @Override
        public RSVPStatus convertToEntityAttribute(String value) {
            if (value == null) {
                return null;
            }
            return RSVPStatus.fromString(value);
        }
    }
    
    @Override
    public String toString() {
        return "RSVP{" +
                "id=" + id +
                ", guestId=" + guestId +
                ", status=" + status +
                ", plusOneAttending=" + plusOneAttending +
                ", submittedAt=" + submittedAt +
                '}';
    }
} 