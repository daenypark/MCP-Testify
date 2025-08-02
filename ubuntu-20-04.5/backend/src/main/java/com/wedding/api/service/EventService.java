package com.wedding.api.service;

import com.wedding.api.model.WeddingEvent;
import com.wedding.api.repository.WeddingEventRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {
    
    @Autowired
    private WeddingEventRepository eventRepository;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    private GuestService guestService;
    
    @Autowired
    private RSVPService rsvpService;
    
    @Timed(value = "wedding.service.events.details", description = "Time to get event details")
    @Cacheable(value = "events", key = "'details'")
    public WeddingEvent getEventDetails() {
        // Simulate database lookup delay
        simulateProcessingDelay(100, 300);
        
        List<WeddingEvent> events = eventRepository.findAllOrderedByDate();
        if (events.isEmpty()) {
            // Return default event if none exist
            WeddingEvent defaultEvent = new WeddingEvent("Wedding Ceremony", LocalDateTime.now().plusDays(30));
            defaultEvent.setDescription("Join us for our special day!");
            defaultEvent.setVenueName("Beautiful Gardens");
            defaultEvent.setVenueAddress("123 Garden Lane, City, State");
            defaultEvent.setDressCode("Formal");
            return defaultEvent;
        }
        
        return events.get(0);
    }
    
    @Timed(value = "wedding.service.events.dashboard", description = "Time to get dashboard statistics")
    public Map<String, Object> getDashboardStats() {
        // This method combines multiple service calls for comprehensive stats
        Map<String, Object> stats = new HashMap<>();
        
        // Get guest statistics
        long totalGuests = guestService.getTotalGuestsCount();
        long plusOneGuests = guestService.getPlusOneGuestsCount();
        
        // Get RSVP statistics
        Map<String, Object> rsvpStats = rsvpService.calculateRSVPStats();
        
        // Get event count
        long totalEvents = eventRepository.count();
        
        stats.put("totalGuests", totalGuests);
        stats.put("plusOneGuests", plusOneGuests);
        stats.put("totalEvents", totalEvents);
        stats.put("rsvpStats", rsvpStats);
        stats.put("lastUpdated", LocalDateTime.now());
        
        // Record dashboard access metric
        meterRegistry.counter("wedding.dashboard.accessed.total").increment();
        
        return stats;
    }
    
    @Timed(value = "wedding.service.events.cpu.intensive", description = "CPU intensive task for performance testing")
    public Map<String, Object> performCPUIntensiveTask() {
        // CPU intensive calculation for APM testing
        long iterations = 1_000_000;
        long startTime = System.currentTimeMillis();
        
        double result = 0;
        for (long i = 0; i < iterations; i++) {
            result += Math.sqrt(i) * Math.sin(i) * Math.cos(i);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        Map<String, Object> response = new HashMap<>();
        response.put("iterations", iterations);
        response.put("result", result);
        response.put("durationMs", duration);
        response.put("timestamp", LocalDateTime.now());
        
        // Record performance metric
        meterRegistry.timer("wedding.performance.cpu.duration").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        return response;
    }
    
    @Timed(value = "wedding.service.events.memory.test", description = "Memory allocation test")
    public String performMemoryTest(int sizeMB) {
        // Memory allocation test for monitoring
        long startTime = System.currentTimeMillis();
        
        try {
            // Allocate memory
            List<byte[]> memoryBlocks = new ArrayList<>();
            int blockSize = 1024 * 1024; // 1MB blocks
            
            for (int i = 0; i < sizeMB; i++) {
                byte[] block = new byte[blockSize];
                // Fill with some data to ensure allocation
                for (int j = 0; j < blockSize; j += 1000) {
                    block[j] = (byte) (j % 256);
                }
                memoryBlocks.add(block);
            }
            
            // Hold memory for a short time
            Thread.sleep(1000);
            
            // Clear memory
            memoryBlocks.clear();
            System.gc(); // Suggest garbage collection
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // Record memory usage metrics
            meterRegistry.gauge("wedding.performance.memory.allocated.mb", sizeMB);
            meterRegistry.timer("wedding.performance.memory.duration").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            return String.format("Successfully allocated and freed %d MB in %d ms", sizeMB, duration);
            
        } catch (OutOfMemoryError e) {
            meterRegistry.counter("wedding.performance.memory.oom.total").increment();
            throw new RuntimeException("Out of memory error during test", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Memory test interrupted", e);
        }
    }
    
    public List<WeddingEvent> getAllEvents() {
        return eventRepository.findAllOrderedByDate();
    }
    
    public List<WeddingEvent> getUpcomingEvents() {
        return eventRepository.findByEventDateAfter(LocalDateTime.now());
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