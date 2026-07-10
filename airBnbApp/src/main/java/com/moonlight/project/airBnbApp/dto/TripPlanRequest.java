package com.moonlight.project.airBnbApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TripPlanRequest {

    @NotBlank(message = "city is required")
    private String city;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private Integer numberOfGuests;

    /** Free-form interests, e.g. "food", "history", "nightlife", "nature". */
    private List<String> interests;

    /** BUDGET, MODERATE, or LUXURY. Free text is tolerated. */
    private String budgetLevel;

    /** Optional — when present the plan is tailored around this specific hotel. */
    private Long hotelId;
}
