package com.wedding.api.service;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class ExternalApiService {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Value("${wedding.external-apis.weather.timeout:5000}")
    private int weatherApiTimeout;
    
    @Value("${wedding.external-apis.maps.timeout:3000}")
    private int mapsApiTimeout;
    
    private final WebClient webClient;
    private final Random random = new Random();
    
    public ExternalApiService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
    
    @Timed(value = "wedding.service.external.weather", description = "Time to get weather data")
    public Map<String, Object> getWeatherForDate(String date) {
        // Simulate external weather API call
        long startTime = System.currentTimeMillis();
        
        try {
            // Simulate API processing time
            simulateApiDelay(500, 2000);
            
            // Simulate occasional API failures
            if (random.nextInt(10) == 0) { // 10% failure rate
                meterRegistry.counter("wedding.external.weather.errors.total").increment();
                throw new RuntimeException("Weather API temporarily unavailable");
            }
            
            // Create mock weather data
            Map<String, Object> weatherData = new HashMap<>();
            weatherData.put("date", date);
            weatherData.put("temperature", 72 + random.nextInt(20)); // 72-92°F
            weatherData.put("condition", getRandomWeatherCondition());
            weatherData.put("humidity", 40 + random.nextInt(40)); // 40-80%
            weatherData.put("windSpeed", 5 + random.nextInt(15)); // 5-20 mph
            weatherData.put("precipitation", random.nextInt(20)); // 0-20%
            weatherData.put("timestamp", LocalDateTime.now());
            weatherData.put("source", "Mock Weather API");
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Record success metrics
            meterRegistry.counter("wedding.external.weather.success.total").increment();
            meterRegistry.timer("wedding.external.weather.duration").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            return weatherData;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            meterRegistry.counter("wedding.external.weather.errors.total").increment();
            meterRegistry.timer("wedding.external.weather.duration").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
            throw new RuntimeException("Failed to fetch weather data: " + e.getMessage(), e);
        }
    }
    
    @Timed(value = "wedding.service.external.directions", description = "Time to get directions")
    public Map<String, Object> getDirections(String from) {
        // Simulate external maps API call
        long startTime = System.currentTimeMillis();
        
        try {
            // Simulate API processing time
            simulateApiDelay(300, 1500);
            
            // Simulate occasional API failures
            if (random.nextInt(15) == 0) { // ~7% failure rate
                meterRegistry.counter("wedding.external.maps.errors.total").increment();
                throw new RuntimeException("Maps API rate limit exceeded");
            }
            
            // Create mock directions data
            Map<String, Object> directionsData = new HashMap<>();
            directionsData.put("from", from);
            directionsData.put("to", "Beautiful Gardens, 123 Garden Lane, City, State");
            directionsData.put("distance", String.format("%.1f miles", 5.0 + random.nextDouble() * 20)); // 5-25 miles
            directionsData.put("duration", String.format("%d minutes", 15 + random.nextInt(45))); // 15-60 minutes
            directionsData.put("route", "Take Main St to Garden Lane, turn right");
            directionsData.put("traffic", getRandomTrafficCondition());
            directionsData.put("timestamp", LocalDateTime.now());
            directionsData.put("source", "Mock Maps API");
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Record success metrics
            meterRegistry.counter("wedding.external.maps.success.total").increment();
            meterRegistry.timer("wedding.external.maps.duration").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            return directionsData;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            meterRegistry.counter("wedding.external.maps.errors.total").increment();
            meterRegistry.timer("wedding.external.maps.duration").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
            throw new RuntimeException("Failed to fetch directions: " + e.getMessage(), e);
        }
    }
    
    // Simulate timeout scenario for APM testing
    public void simulateTimeout() {
        try {
            // Simulate extremely slow API response
            Thread.sleep(10000); // 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("External API timeout simulation interrupted", e);
        }
        throw new RuntimeException("External API timeout (simulated)");
    }
    
    // Real external API call example (for future implementation)
    @Timed(value = "wedding.service.external.real.api", description = "Time for real external API call")
    public Mono<String> callRealExternalApi(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(weatherApiTimeout))
                .doOnSuccess(response -> {
                    meterRegistry.counter("wedding.external.real.success.total").increment();
                })
                .doOnError(WebClientException.class, error -> {
                    meterRegistry.counter("wedding.external.real.errors.total").increment();
                });
    }
    
    // Utility methods
    private void simulateApiDelay(int minMs, int maxMs) {
        try {
            int delay = minMs + (int) (Math.random() * (maxMs - minMs));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private String getRandomWeatherCondition() {
        String[] conditions = {"Sunny", "Partly Cloudy", "Cloudy", "Light Rain", "Clear", "Overcast"};
        return conditions[random.nextInt(conditions.length)];
    }
    
    private String getRandomTrafficCondition() {
        String[] conditions = {"Light Traffic", "Moderate Traffic", "Heavy Traffic", "Clear Roads"};
        return conditions[random.nextInt(conditions.length)];
    }
} 