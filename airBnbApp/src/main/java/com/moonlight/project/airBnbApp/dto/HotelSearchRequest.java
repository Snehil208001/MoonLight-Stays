package com.moonlight.project.airBnbApp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HotelSearchRequest {
    private String city;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private Integer roomsCount;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private String roomType;
    private String amenity;

    private Integer page = 0;
    private Integer size = 10;
}