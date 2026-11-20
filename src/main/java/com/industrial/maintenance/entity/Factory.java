package com.industrial.maintenance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "factories")
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Factory code is required")
    @Column(nullable = false, unique = true)
    private String factoryCode;     // e.g. FAC-BLR

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;            // e.g. Bangalore Plant

    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;            // e.g. Bangalore

    private String address;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFactoryCode() {
        return factoryCode;
    }

    public void setFactoryCode(String factoryCode) {
        this.factoryCode = factoryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}