package com.moonlight.project.airBnbApp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moonlight.project.airBnbApp.dto.HotelDto;
import com.moonlight.project.airBnbApp.dto.TripPlanRequest;
import com.moonlight.project.airBnbApp.dto.TripPlanResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TripPlannerServiceImpl implements TripPlannerService {

    private final HotelService hotelService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public TripPlannerServiceImpl(
            HotelService hotelService,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.model:gemini-2.0-flash}") String model,
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl) {
        this.hotelService = hotelService;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public TripPlanResponse generateTripPlan(TripPlanRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI trip planner is not configured. Please set GEMINI_API_KEY.");
        }

        String prompt = buildPrompt(request);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "responseMimeType", "application/json",
                        "responseSchema", buildResponseSchema()));

        String rawJson = callGemini(body);

        try {
            return objectMapper.readValue(rawJson, TripPlanResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse Gemini trip plan response: {}", e.getMessage());
            throw new IllegalStateException("The trip planner returned an unexpected response. Please try again.");
        }
    }

    @SuppressWarnings("unchecked")
    private String callGemini(Map<String, Object> body) {
        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new IllegalStateException("The AI trip planner is temporarily unavailable. Please try again shortly.");
        }

        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String text = (String) parts.get(0).get("text");
            if (text == null || text.isBlank()) {
                throw new IllegalStateException("empty text");
            }
            return text;
        } catch (Exception e) {
            log.error("Unexpected Gemini response shape: {}", response);
            throw new IllegalStateException("The trip planner returned an unexpected response. Please try again.");
        }
    }

    private String buildPrompt(TripPlanRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert local travel guide for the Moonlight Stays hotel booking app. ");
        sb.append("Create a practical, day-by-day travel itinerary as JSON matching the provided schema.\n\n");

        sb.append("Destination city: ").append(request.getCity()).append("\n");

        int nights = resolveNights(request);
        sb.append("Number of days to plan: ").append(nights).append("\n");
        if (request.getCheckInDate() != null) {
            sb.append("Check-in date: ").append(request.getCheckInDate()).append("\n");
        }
        if (request.getCheckOutDate() != null) {
            sb.append("Check-out date: ").append(request.getCheckOutDate()).append("\n");
        }
        if (request.getNumberOfGuests() != null && request.getNumberOfGuests() > 0) {
            sb.append("Number of travellers: ").append(request.getNumberOfGuests()).append("\n");
        }
        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            sb.append("Traveller interests: ").append(String.join(", ", request.getInterests())).append("\n");
        }
        if (request.getBudgetLevel() != null && !request.getBudgetLevel().isBlank()) {
            sb.append("Budget level: ").append(request.getBudgetLevel()).append("\n");
        }

        appendHotelContext(sb, request);

        sb.append("\nGuidelines:\n");
        sb.append("- Generate exactly ").append(nights).append(" day(s).\n");
        sb.append("- For each day provide 2-4 activities across Morning, Afternoon and Evening.\n");
        sb.append("- Recommend real, well-known places and neighbourhoods in the destination city.\n");
        sb.append("- Keep descriptions concise (1-2 sentences) and actionable.\n");
        sb.append("- Include a meal suggestion for each day and a short list of overall travel tips.\n");
        sb.append("- Respect the traveller's interests and budget level where given.\n");
        return sb.toString();
    }

    private void appendHotelContext(StringBuilder sb, TripPlanRequest request) {
        if (request.getHotelId() == null) {
            return;
        }
        try {
            HotelDto hotel = hotelService.getHotelById(request.getHotelId());
            sb.append("\nThe traveller is staying at this hotel — anchor the itinerary around it:\n");
            sb.append("- Hotel name: ").append(hotel.getName()).append("\n");
            if (hotel.getCity() != null) {
                sb.append("- Hotel city: ").append(hotel.getCity()).append("\n");
            }
            if (hotel.getAmenities() != null && hotel.getAmenities().length > 0) {
                sb.append("- Hotel amenities: ").append(String.join(", ", hotel.getAmenities())).append("\n");
            }
            if (hotel.getContactInfo() != null && hotel.getContactInfo().getAddress() != null) {
                sb.append("- Hotel address: ").append(hotel.getContactInfo().getAddress()).append("\n");
            }
        } catch (Exception e) {
            // Hotel enrichment is best-effort; fall back to a generic city itinerary.
            log.warn("Could not enrich trip plan with hotel {}: {}", request.getHotelId(), e.getMessage());
        }
    }

    private int resolveNights(TripPlanRequest request) {
        if (request.getCheckInDate() != null && request.getCheckOutDate() != null) {
            long days = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
            if (days >= 1) {
                return (int) Math.min(days, 14); // cap to keep responses bounded
            }
        }
        return 3; // sensible default when dates are missing
    }

    /** OpenAPI-subset schema Gemini uses to force structured JSON output. */
    private Map<String, Object> buildResponseSchema() {
        Map<String, Object> activity = objectSchema(new LinkedHashMap<>() {{
            put("timeOfDay", stringSchema());
            put("title", stringSchema());
            put("description", stringSchema());
        }}, List.of("timeOfDay", "title", "description"));

        Map<String, Object> dayPlan = objectSchema(new LinkedHashMap<>() {{
            put("day", Map.of("type", "INTEGER"));
            put("title", stringSchema());
            put("activities", arraySchema(activity));
            put("mealSuggestion", stringSchema());
        }}, List.of("day", "title", "activities"));

        return objectSchema(new LinkedHashMap<>() {{
            put("destination", stringSchema());
            put("summary", stringSchema());
            put("days", arraySchema(dayPlan));
            put("tips", arraySchema(stringSchema()));
        }}, List.of("destination", "summary", "days"));
    }

    private Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "OBJECT");
        schema.put("properties", properties);
        schema.put("required", new ArrayList<>(required));
        return schema;
    }

    private Map<String, Object> arraySchema(Map<String, Object> items) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "ARRAY");
        schema.put("items", items);
        return schema;
    }

    private Map<String, Object> stringSchema() {
        return Map.of("type", "STRING");
    }
}
