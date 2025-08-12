package com.example.service_user.model;

public class User {
    private Long id;
    private String name;

    // Constructor
    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}