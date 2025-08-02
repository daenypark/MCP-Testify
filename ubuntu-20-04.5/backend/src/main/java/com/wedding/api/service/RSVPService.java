package com.wedding.api.service;

import com.wedding.api.model.RSVP;
import com.wedding.api.repository.RSVPRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RSVPService {
    
    @Autowired
    private RSVPRepository rsvpRepository;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Timed(value = "wedding.service.rsvp.submit", description = "Time to submit RSVP")
    public RSVP submitRSVP(RSVP rsvp) {
        // Simulate complex business logic
        simulateProcessingDelay(200, 500);
        
        // Check if RSVP already exists for this guest
        rsvpRepository.findByGuestId(rsvp.getGuestId())
                .ifPresent(existingRSVP -> {
                    throw new RuntimeException("RSVP already exists for guest ID: " + rsvp.getGuestId());
                });
        
        RSVP savedRSVP = rsvpRepository.save(rsvp);
        
        // Record custom metrics based on status
        meterRegistry.counter("wedding.rsvp.submitted.total", 
                "status", savedRSVP.getStatus().toString()).increment();
        
        return savedRSVP;
    }
    
    @Timed(value = "wedding.service.rsvp.get", description = "Time to get RSVP by guest ID")
    public RSVP getRSVPByGuestId(Long guestId) {
        return rsvpRepository.findByGuestId(guestId)
                .orElseThrow(() -> new RuntimeException("RSVP not found for guest ID: " + guestId));
    }
    
    @Timed(value = "wedding.service.rsvp.stats", description = "Time to calculate RSVP statistics")
    public Map<String, Object> calculateRSVPStats() {
        // Simulate complex aggregation processing
        simulateProcessingDelay(300, 700);
        
        Map<String, Object> stats = new HashMap<>();
        
        long totalRSVPs = rsvpRepository.count();
        long attendingCount = rsvpRepository.countByStatus(RSVP.RSVPStatus.ATTENDING);
        long notAttendingCount = rsvpRepository.countByStatus(RSVP.RSVPStatus.NOT_ATTENDING);
        long maybeCount = rsvpRepository.countByStatus(RSVP.RSVPStatus.MAYBE);
        long plusOneCount = rsvpRepository.countByPlusOneAttendingTrue();
        
        stats.put("totalRSVPs", totalRSVPs);
        stats.put("attending", attendingCount);
        stats.put("notAttending", notAttendingCount);
        stats.put("maybe", maybeCount);
        stats.put("plusOneAttending", plusOneCount);
        stats.put("attendanceRate", totalRSVPs > 0 ? (double) attendingCount / totalRSVPs * 100 : 0);
        
        // Record gauge metrics
        meterRegistry.gauge("wedding.rsvp.total", totalRSVPs);
        meterRegistry.gauge("wedding.rsvp.attending", attendingCount);
        meterRegistry.gauge("wedding.rsvp.attendance.rate", 
                totalRSVPs > 0 ? (double) attendingCount / totalRSVPs * 100 : 0);
        
        return stats;
    }
    
    @Async
    @Timed(value = "wedding.service.rsvp.email", description = "Time to send confirmation email")
    public void sendConfirmationEmail(RSVP rsvp) {
        // Simulate email sending delay
        simulateProcessingDelay(1000, 2000);
        
        // Simulate email service integration
        System.out.println("Sending confirmation email for RSVP ID: " + rsvp.getId());
        
        // Record email metrics
        meterRegistry.counter("wedding.email.sent.total", "type", "rsvp_confirmation").increment();
    }
    
    public List<RSVP> getRSVPsByStatus(RSVP.RSVPStatus status) {
        return rsvpRepository.findByStatus(status);
    }
    
    // Utility method to simulate processing time
    private void simulateProcessingDelay(int minMs, int maxMs) {
        try {
            int delay = minMs + (int) (Math.random() * (maxMs - minMs));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 