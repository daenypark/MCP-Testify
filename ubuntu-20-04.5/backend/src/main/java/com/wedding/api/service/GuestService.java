package com.wedding.api.service;

import com.wedding.api.model.Guest;
import com.wedding.api.repository.GuestRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GuestService {
    
    @Autowired
    private GuestRepository guestRepository;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Timed(value = "wedding.service.guests.get", description = "Time to get paginated guests")
    public Page<Guest> getGuests(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return guestRepository.findBySearchTerm(search.trim(), pageable);
        }
        return guestRepository.findAll(pageable);
    }
    
    @Timed(value = "wedding.service.guests.create", description = "Time to create guest")
    public Guest createGuest(Guest guest) {
        // Simulate business logic processing time
        simulateProcessingDelay(100, 300);
        
        // Check for duplicate email
        Optional<Guest> existingGuest = guestRepository.findByEmail(guest.getEmail());
        if (existingGuest.isPresent()) {
            throw new RuntimeException("Guest with email " + guest.getEmail() + " already exists");
        }
        
        Guest savedGuest = guestRepository.save(guest);
        
        // Record custom metric
        meterRegistry.counter("wedding.guests.created.total").increment();
        
        return savedGuest;
    }
    
    @Timed(value = "wedding.service.guests.get.single", description = "Time to get single guest")
    @Cacheable(value = "guests", key = "#id")
    public Guest getGuestById(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guest not found with id: " + id));
    }
    
    @Timed(value = "wedding.service.guests.update", description = "Time to update guest")
    public Guest updateGuest(Long id, Guest guestDetails) {
        Guest guest = getGuestById(id);
        
        // Simulate complex business logic
        simulateProcessingDelay(150, 400);
        
        guest.setFirstName(guestDetails.getFirstName());
        guest.setLastName(guestDetails.getLastName());
        guest.setEmail(guestDetails.getEmail());
        guest.setPhone(guestDetails.getPhone());
        guest.setAddress(guestDetails.getAddress());
        guest.setPlusOne(guestDetails.getPlusOne());
        guest.setDietaryRestrictions(guestDetails.getDietaryRestrictions());
        
        Guest updatedGuest = guestRepository.save(guest);
        
        // Record custom metric
        meterRegistry.counter("wedding.guests.updated.total").increment();
        
        return updatedGuest;
    }
    
    @Timed(value = "wedding.service.guests.delete", description = "Time to delete guest")
    public void deleteGuest(Long id) {
        Guest guest = getGuestById(id);
        guestRepository.delete(guest);
        
        // Record custom metric
        meterRegistry.counter("wedding.guests.deleted.total").increment();
    }
    
    @Timed(value = "wedding.service.guests.slow.query", description = "Intentionally slow query for APM testing")
    public List<Guest> performSlowQuery() {
        // This will generate a slow database trace
        return guestRepository.findGuestsWithSlowQuery();
    }
    
    @Timed(value = "wedding.service.guests.random", description = "Time to get random guests")
    public List<Guest> getRandomGuests() {
        return guestRepository.findRandomGuests();
    }
    
    // Error simulation for APM testing
    public void simulateDatabaseError() {
        throw new RuntimeException("Simulated database connection error");
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
    
    // Business metrics
    public long getTotalGuestsCount() {
        return guestRepository.count();
    }
    
    public long getPlusOneGuestsCount() {
        return guestRepository.countByPlusOneTrue();
    }
} 