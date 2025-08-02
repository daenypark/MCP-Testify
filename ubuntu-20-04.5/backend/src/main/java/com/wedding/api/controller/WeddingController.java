package com.wedding.api.controller;

import com.wedding.api.model.Guest;
import com.wedding.api.model.RSVP;
import com.wedding.api.model.WeddingEvent;
import com.wedding.api.service.GuestService;
import com.wedding.api.service.RSVPService;
import com.wedding.api.service.EventService;
import com.wedding.api.service.ExternalApiService;
import com.wedding.api.service.MetricsService;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.annotation.Counted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@Validated
@CrossOrigin(origins = "*")
public class WeddingController {

    @Autowired
    private GuestService guestService;
    
    @Autowired
    private RSVPService rsvpService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private ExternalApiService externalApiService;
    
    @Autowired
    private MetricsService metricsService;

    // ==== GUEST MANAGEMENT APIs (Database Heavy) ====
    
    @GetMapping("/guests")
    @Timed(value = "wedding.guests.list.time", description = "Time to list guests")
    @Counted(value = "wedding.guests.list.count", description = "Number of guest list requests")
    public ResponseEntity<Page<Guest>> getGuests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        
        metricsService.recordGuestListAccess();
        Page<Guest> guests = guestService.getGuests(pageable, search);
        return ResponseEntity.ok(guests);
    }

    @PostMapping("/guests")
    @Timed(value = "wedding.guests.create.time", description = "Time to create guest")
    @Counted(value = "wedding.guests.create.count", description = "Number of guest creations")
    public ResponseEntity<Guest> createGuest(@Valid @RequestBody Guest guest) {
        metricsService.recordGuestCreation();
        Guest savedGuest = guestService.createGuest(guest);
        return ResponseEntity.ok(savedGuest);
    }

    @GetMapping("/guests/{id}")
    @Timed(value = "wedding.guests.get.time", description = "Time to get single guest")
    @Counted(value = "wedding.guests.get.count", description = "Number of single guest requests")
    public ResponseEntity<Guest> getGuest(@PathVariable Long id) {
        Guest guest = guestService.getGuestById(id);
        return ResponseEntity.ok(guest);
    }

    @PutMapping("/guests/{id}")
    @Timed(value = "wedding.guests.update.time", description = "Time to update guest")
    @Counted(value = "wedding.guests.update.count", description = "Number of guest updates")
    public ResponseEntity<Guest> updateGuest(@PathVariable Long id, @Valid @RequestBody Guest guest) {
        Guest updatedGuest = guestService.updateGuest(id, guest);
        return ResponseEntity.ok(updatedGuest);
    }

    @DeleteMapping("/guests/{id}")
    @Timed(value = "wedding.guests.delete.time", description = "Time to delete guest")
    @Counted(value = "wedding.guests.delete.count", description = "Number of guest deletions")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.noContent().build();
    }

    // ==== RSVP APIs (Business Logic + Async Processing) ====
    
    @PostMapping("/rsvp/submit")
    @Timed(value = "wedding.rsvp.submit.time", description = "Time to submit RSVP")
    @Counted(value = "wedding.rsvp.submit.count", description = "Number of RSVP submissions")
    public ResponseEntity<RSVP> submitRSVP(@Valid @RequestBody RSVP rsvp) {
        metricsService.recordRSVPSubmission(rsvp.getStatus());
        
        // Simulate complex business logic with multiple service calls
        RSVP savedRSVP = rsvpService.submitRSVP(rsvp);
        
        // Async email notification (generates separate span)
        CompletableFuture.runAsync(() -> {
            rsvpService.sendConfirmationEmail(savedRSVP);
        });
        
        return ResponseEntity.ok(savedRSVP);
    }

    @GetMapping("/rsvp/{guestId}")
    @Timed(value = "wedding.rsvp.get.time", description = "Time to get RSVP")
    @Counted(value = "wedding.rsvp.get.count", description = "Number of RSVP requests")
    public ResponseEntity<RSVP> getRSVP(@PathVariable Long guestId) {
        RSVP rsvp = rsvpService.getRSVPByGuestId(guestId);
        return ResponseEntity.ok(rsvp);
    }

    @GetMapping("/rsvp/stats")
    @Timed(value = "wedding.rsvp.stats.time", description = "Time to calculate RSVP stats")
    @Counted(value = "wedding.rsvp.stats.count", description = "Number of RSVP stats requests")
    public ResponseEntity<Map<String, Object>> getRSVPStats() {
        Map<String, Object> stats = rsvpService.calculateRSVPStats();
        metricsService.recordStatsAccess();
        return ResponseEntity.ok(stats);
    }

    // ==== EVENT APIs (Caching + External Services) ====
    
    @GetMapping("/events/details")
    @Timed(value = "wedding.events.details.time", description = "Time to get event details")
    @Counted(value = "wedding.events.details.count", description = "Number of event detail requests")
    public ResponseEntity<WeddingEvent> getEventDetails() {
        // This endpoint uses Redis caching
        WeddingEvent eventDetails = eventService.getEventDetails();
        metricsService.recordEventDetailsAccess();
        return ResponseEntity.ok(eventDetails);
    }

    @GetMapping("/weather/{date}")
    @Timed(value = "wedding.weather.get.time", description = "Time to get weather")
    @Counted(value = "wedding.weather.get.count", description = "Number of weather requests")
    public ResponseEntity<Map<String, Object>> getWeather(@PathVariable String date) {
        // External API call that can be slow/fail
        Map<String, Object> weather = externalApiService.getWeatherForDate(date);
        metricsService.recordWeatherApiCall();
        return ResponseEntity.ok(weather);
    }

    @GetMapping("/venue/directions")
    @Timed(value = "wedding.venue.directions.time", description = "Time to get directions")
    @Counted(value = "wedding.venue.directions.count", description = "Number of directions requests")
    public ResponseEntity<Map<String, Object>> getVenueDirections(
            @RequestParam String from) {
        // Another external API call
        Map<String, Object> directions = externalApiService.getDirections(from);
        return ResponseEntity.ok(directions);
    }

    // ==== ANALYTICS APIs (Custom Metrics Generation) ====
    
    @PostMapping("/analytics/page-view")
    @Counted(value = "wedding.analytics.pageview.count", description = "Number of page views")
    public ResponseEntity<Void> recordPageView(@RequestBody Map<String, String> pageData) {
        String page = pageData.get("page");
        String userAgent = pageData.get("userAgent");
        
        metricsService.recordPageView(page, userAgent);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/analytics/rsvp-funnel")
    @Counted(value = "wedding.analytics.funnel.count", description = "Number of funnel events")
    public ResponseEntity<Void> recordFunnelStep(@RequestBody Map<String, String> funnelData) {
        String step = funnelData.get("step");
        String guestId = funnelData.get("guestId");
        
        metricsService.recordFunnelStep(step, guestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard/stats")
    @Timed(value = "wedding.dashboard.stats.time", description = "Time to get dashboard stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        // Complex aggregation with multiple service calls
        Map<String, Object> stats = eventService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // ==== PERFORMANCE TEST ENDPOINTS ====
    
    @GetMapping("/performance/slow-query")
    @Timed(value = "wedding.performance.slow.time", description = "Intentionally slow database query")
    public ResponseEntity<List<Guest>> slowDatabaseQuery() {
        // Intentionally slow query for APM testing
        List<Guest> guests = guestService.performSlowQuery();
        return ResponseEntity.ok(guests);
    }

    @GetMapping("/performance/cpu-intensive")
    @Timed(value = "wedding.performance.cpu.time", description = "CPU intensive operation")
    public ResponseEntity<Map<String, Object>> cpuIntensiveOperation() {
        // CPU intensive operation for monitoring
        Map<String, Object> result = eventService.performCPUIntensiveTask();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/performance/memory-test")
    @Timed(value = "wedding.performance.memory.time", description = "Memory usage test")
    public ResponseEntity<String> memoryTest(@RequestParam(defaultValue = "100") int sizeMB) {
        // Memory allocation test
        String result = eventService.performMemoryTest(sizeMB);
        return ResponseEntity.ok(result);
    }

    // ==== ERROR SIMULATION ENDPOINTS ====
    
    @GetMapping("/errors/database-error")
    public ResponseEntity<String> simulateDatabaseError() {
        guestService.simulateDatabaseError();
        return ResponseEntity.ok("This shouldn't return");
    }

    @GetMapping("/errors/external-api-timeout")
    public ResponseEntity<String> simulateExternalApiTimeout() {
        externalApiService.simulateTimeout();
        return ResponseEntity.ok("This shouldn't return");
    }

    @GetMapping("/errors/validation-error")
    public ResponseEntity<Guest> simulateValidationError() {
        Guest invalidGuest = new Guest();
        // This will trigger validation errors
        return ResponseEntity.ok(guestService.createGuest(invalidGuest));
    }
} 