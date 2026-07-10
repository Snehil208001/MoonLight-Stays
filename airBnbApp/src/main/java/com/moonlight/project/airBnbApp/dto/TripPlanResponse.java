package com.moonlight.project.airBnbApp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Structured AI-generated itinerary. Populated by Gemini via a response schema so
 * the shape here matches exactly what the model is asked to return.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripPlanResponse {

    private String destination;
    private String summary;
    private List<DayPlan> days;
    private List<String> tips;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DayPlan {
        private Integer day;
        private String title;
        private List<Activity> activities;
        private String mealSuggestion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Activity {
        /** Morning, Afternoon, or Evening. */
        private String timeOfDay;
        private String title;
        private String description;
    }
}
