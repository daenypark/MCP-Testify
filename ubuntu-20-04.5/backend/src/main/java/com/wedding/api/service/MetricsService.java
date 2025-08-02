package com.wedding.api.service;

import com.wedding.api.model.RSVP;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    // Counters for tracking various events
    private final Counter guestListAccessCounter;
    private final Counter guestCreationCounter;
    private final Counter statsAccessCounter;
    private final Counter weatherApiCallCounter;
    private final Counter eventDetailsAccessCounter;
    
    // Timers for performance tracking
    private final Timer pageViewTimer;
    private final Timer rsvpSubmissionTimer;
    
    // Simple counters for tracking
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong totalPageViews = new AtomicLong(0);
    
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.guestListAccessCounter = Counter.builder("wedding.metrics.guest.list.access")
                .description("Number of times guest list was accessed")
                .register(meterRegistry);
                
        this.guestCreationCounter = Counter.builder("wedding.metrics.guest.creation")
                .description("Number of guests created")
                .register(meterRegistry);
                
        this.statsAccessCounter = Counter.builder("wedding.metrics.stats.access")
                .description("Number of times stats were accessed")
                .register(meterRegistry);
                
        this.weatherApiCallCounter = Counter.builder("wedding.metrics.weather.api.calls")
                .description("Number of weather API calls")
                .register(meterRegistry);
                
        this.eventDetailsAccessCounter = Counter.builder("wedding.metrics.event.details.access")
                .description("Number of times event details were accessed")
                .register(meterRegistry);
        
        // Initialize timers
        this.pageViewTimer = Timer.builder("wedding.metrics.page.view.duration")
                .description("Time spent processing page views")
                .register(meterRegistry);
                
        this.rsvpSubmissionTimer = Timer.builder("wedding.metrics.rsvp.submission.duration")
                .description("Time spent processing RSVP submissions")
                .register(meterRegistry);
    }
    
    // Business event recording methods
    public void recordGuestListAccess() {
        guestListAccessCounter.increment();
    }
    
    public void recordGuestCreation() {
        guestCreationCounter.increment();
    }
    
    public void recordStatsAccess() {
        statsAccessCounter.increment();
    }
    
    public void recordWeatherApiCall() {
        weatherApiCallCounter.increment();
    }
    
    public void recordEventDetailsAccess() {
        eventDetailsAccessCounter.increment();
    }
    
    public void recordRSVPSubmission(RSVP.RSVPStatus status) {
        // Record overall RSVP submission
        try {
            rsvpSubmissionTimer.recordCallable(() -> {
                // Simulate processing time
                Thread.sleep(50);
                return null;
            });
        } catch (Exception e) {
            // Ignore timing errors
        }
        
        // Record by status using simple counters
        String statusKey = status.toString().toLowerCase();
        meterRegistry.counter("wedding.rsvp.submitted", "status", statusKey).increment();
    }
    
    public void recordPageView(String page, String userAgent) {
        // Record overall page view
        try {
            pageViewTimer.recordCallable(() -> {
                totalPageViews.incrementAndGet();
                
                // Extract browser from user agent for analytics
                String browser = extractBrowser(userAgent);
                meterRegistry.counter("wedding.page.views", 
                        "page", page, 
                        "browser", browser).increment();
                
                return null;
            });
        } catch (Exception e) {
            // Just increment the counter if timing fails
            meterRegistry.counter("wedding.page.views", "page", page).increment();
        }
    }
    
    public void recordFunnelStep(String step, String guestId) {
        // Record with tags using simple counters
        meterRegistry.counter("wedding.funnel.step", 
                "step", step,
                "timestamp", String.valueOf(System.currentTimeMillis() / 1000)).increment();
    }
    
    // User activity tracking
    public void recordUserActivity(String userId) {
        activeUsers.incrementAndGet();
        
        // Record custom counter
        meterRegistry.counter("wedding.user.activity", "user", userId).increment();
    }
    
    // Performance metrics
    public void recordResponseTime(String endpoint, long durationMs) {
        meterRegistry.timer("wedding.endpoint.response.time", "endpoint", endpoint)
                .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    
    public void recordDatabaseQueryTime(String queryType, long durationMs) {
        meterRegistry.timer("wedding.database.query.time", "type", queryType)
                .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    
    public void recordCacheHit(String cacheKey, boolean hit) {
        meterRegistry.counter("wedding.cache.access", 
                "key", cacheKey, 
                "result", hit ? "hit" : "miss").increment();
    }
    
    // Error tracking
    public void recordError(String errorType, String endpoint) {
        meterRegistry.counter("wedding.errors", 
                "type", errorType, 
                "endpoint", endpoint).increment();
    }
    
    // Business metrics
    public void recordBusinessMetric(String metricName, double value, String... tags) {
        // Use simple counter for business metrics
        meterRegistry.counter(metricName, tags).increment(value);
    }
    
    // Utility methods
    private String extractBrowser(String userAgent) {
        if (userAgent == null) return "unknown";
        
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("chrome")) return "chrome";
        if (userAgent.contains("firefox")) return "firefox";
        if (userAgent.contains("safari")) return "safari";
        if (userAgent.contains("edge")) return "edge";
        return "other";
    }
    
    // Periodic metrics reset (for demonstration)
    public void resetActiveUsers() {
        activeUsers.set(0);
    }
    
    // Get current metrics summary
    public void logMetricsSummary() {
        System.out.println("=== Wedding App Metrics Summary ===");
        System.out.println("Total Page Views: " + totalPageViews.get());
        System.out.println("Active Users: " + activeUsers.get());
        System.out.println("Guest List Accesses: " + guestListAccessCounter.count());
        System.out.println("Weather API Calls: " + weatherApiCallCounter.count());
        System.out.println("Timestamp: " + LocalDateTime.now());
    }
} 