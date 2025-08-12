package com.example.service_gateway.model;

public class User {
    private Long id;
    private String name;

    public User() {} // Necesario para deserializar JSON

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}