package com.wedding.api.repository;

import com.wedding.api.model.RSVP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RSVPRepository extends JpaRepository<RSVP, Long> {
    
    Optional<RSVP> findByGuestId(Long guestId);
    
    List<RSVP> findByStatus(RSVP.RSVPStatus status);
    
    @Query("SELECT COUNT(r) FROM RSVP r WHERE r.status = :status")
    long countByStatus(RSVP.RSVPStatus status);
    
    @Query("SELECT COUNT(r) FROM RSVP r WHERE r.plusOneAttending = true")
    long countByPlusOneAttendingTrue();
    
    @Query("SELECT r.status, COUNT(r) FROM RSVP r GROUP BY r.status")
    List<Object[]> getStatusCounts();
} 