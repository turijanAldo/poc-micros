package com.example.service_user.controller;

import com.example.service_user.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Map;
@RestController
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    // Métricas personalizadas
    private final Counter userRequestCounter;
    private final Timer userRequestTimer;
    
    public UserController(MeterRegistry meterRegistry) {
        this.userRequestCounter = Counter.builder("user_requests_total")
                .description("Total number of user requests")
                .register(meterRegistry);
                
        this.userRequestTimer = Timer.builder("user_request_duration")
                .description("Duration of user requests")
                .register(meterRegistry);
    }
    
    @GetMapping("/user")
    public User getUser() {
        try {
            return userRequestTimer.recordCallable(() -> {
                userRequestCounter.increment();
                
                logger.info("Received request for user data");
                
                // Simular algo de procesamiento
                Thread.sleep(50); // 50ms delay
                
                User user = new User(1L, "Juan Pérez");
                logger.info("Returning user: {}", user.getName());
                
                return user;
            });
        } catch (Exception e) {
            logger.error("Error processing user request: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving user", e);
        }
    }
    
    @GetMapping("/health-check")
    public java.util.Map<String, String> healthCheck() {
        logger.info("Health check requested");
        return java.util.Map.of("status", "UP", "service", "service-user");
    }
}