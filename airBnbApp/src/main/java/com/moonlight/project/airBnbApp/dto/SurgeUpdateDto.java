package com.moonlight.project.airBnbApp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SurgeUpdateDto {
    private BigDecimal surgeFactor;
    private LocalDate startDate;
    private LocalDate endDate;
}