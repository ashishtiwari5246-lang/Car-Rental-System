package com.shiva.carrentalapplication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "cars")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    private int year;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Builder.Default
    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "license_plate", unique = true)
    private String licensePlate;

    private String color;
    private int seats;
    private String imageUrl;
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    public enum Category {
        ECONOMY, COMPACT, SUV, LUXURY, VAN, CONVERTIBLE
    }
}
