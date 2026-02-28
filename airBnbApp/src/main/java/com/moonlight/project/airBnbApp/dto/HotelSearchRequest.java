package com.moonlight.project.airBnbApp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HotelSearchRequest {
    private String city;
    private LocalDate checkInDate;
    private LocalDate endDate;
    private Integer roomsCount;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // --- NEW: Added for Search by Amenity and Room Type ---
    private String roomType;
    private String amenity;

    private Integer page = 0;
    private Integer size = 10;
}