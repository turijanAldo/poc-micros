package com.company.micros1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync  // Required for @Async methods to work
@EnableScheduling  // If you plan to use @Scheduled methods
@EnableTransactionManagement 
public class Micros1Application {

    public static void main(String[] args) {
        // Add system properties for debugging
        System.setProperty("spring.output.ansi.enabled", "always");
        
        try {
            SpringApplication app = new SpringApplication(Micros1Application.class);
            // Enable debug mode
            app.run(args);
        } catch (Exception e) {
            System.err.println("Application failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}