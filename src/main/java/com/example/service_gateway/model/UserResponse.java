package com.example.service_gateway.model;

public class UserResponse {
    private Long id;
    private String name;
    private String errorMessage;
    public UserResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters necesarios para que Spring Boot serialice esto automáticamente a JSON
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public  UserResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
