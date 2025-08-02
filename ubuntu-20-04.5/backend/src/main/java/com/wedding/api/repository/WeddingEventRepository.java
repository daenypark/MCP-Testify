package com.wedding.api.repository;

import com.wedding.api.model.WeddingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WeddingEventRepository extends JpaRepository<WeddingEvent, Long> {
    
    List<WeddingEvent> findByEventDateAfter(LocalDateTime date);
    
    List<WeddingEvent> findByEventDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT w FROM WeddingEvent w ORDER BY w.eventDate ASC")
    List<WeddingEvent> findAllOrderedByDate();
    
    List<WeddingEvent> findByVenueNameContainingIgnoreCase(String venueName);
} 