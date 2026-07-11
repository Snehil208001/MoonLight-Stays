package com.moonlight.project.airBnbApp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moonlight.project.airBnbApp.dto.HotelDto;
import com.moonlight.project.airBnbApp.dto.TripPlanRequest;
import com.moonlight.project.airBnbApp.dto.TripPlanResponse;
import com.moonlight.project.airBnbApp.entity.HotelContactInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TripPlannerServiceImplTest {

    @Mock
    private HotelService hotelService;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ObjectMapper objectMapper;
    private TripPlannerServiceImpl tripPlannerService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        tripPlannerService = new TripPlannerServiceImpl(
                hotelService,
                objectMapper,
                "dummy-api-key",
                "gemini-3.5-flash",
                "https://generativelanguage.googleapis.com/v1beta"
        );
        ReflectionTestUtils.setField(tripPlannerService, "restClient", restClient);
    }

    @Test
    void generateTripPlan_Success() throws Exception {
        // Arrange
        TripPlanRequest request = new TripPlanRequest();
        request.setCity("Paris");
        request.setCheckInDate(LocalDate.now());
        request.setCheckOutDate(LocalDate.now().plusDays(2));
        request.setInterests(List.of("museums", "food"));
        request.setBudgetLevel("MODERATE");

        // Mock Gemini response structure
        String mockGeminiResponseJson = "{"
                + "\"destination\": \"Paris\","
                + "\"summary\": \"A wonderful trip to Paris.\","
                + "\"days\": ["
                + "  {"
                + "    \"day\": 1,"
                + "    \"title\": \"Day 1 in Paris\","
                + "    \"activities\": ["
                + "      {\"timeOfDay\": \"Morning\", \"title\": \"Louvre Museum\", \"description\": \"Visit the Louvre.\"},"
                + "      {\"timeOfDay\": \"Afternoon\", \"title\": \"Eiffel Tower\", \"description\": \"See Eiffel.\"},"
                + "      {\"timeOfDay\": \"Evening\", \"title\": \"Seine River Cruise\", \"description\": \"Enjoy cruise.\"}"
                + "    ],"
                + "    \"mealSuggestion\": \"French Bistro\""
                + "  }"
                + "],"
                + "\"tips\": [\"Buy museum pass in advance\"]"
                + "}";

        Map<String, Object> geminiResponseBody = Map.of(
                "candidates", List.of(Map.of(
                        "content", Map.of(
                                "parts", List.of(Map.of("text", mockGeminiResponseJson))
                        )
                ))
        );

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(geminiResponseBody);

        // Act
        TripPlanResponse response = tripPlannerService.generateTripPlan(request);

        // Assert
        assertNotNull(response);
        assertEquals("Paris", response.getDestination());
        assertEquals("A wonderful trip to Paris.", response.getSummary());
        assertEquals(1, response.getDays().size());
        assertEquals("Day 1 in Paris", response.getDays().get(0).getTitle());
        assertEquals(3, response.getDays().get(0).getActivities().size());
        assertEquals("French Bistro", response.getDays().get(0).getMealSuggestion());
        assertEquals("Louvre Museum", response.getDays().get(0).getActivities().get(0).getTitle());
        assertEquals(1, response.getTips().size());
        assertEquals("Buy museum pass in advance", response.getTips().get(0));
    }

    @Test
    void generateTripPlan_MissingApiKey() {
        // Arrange
        ReflectionTestUtils.setField(tripPlannerService, "apiKey", null);
        TripPlanRequest request = new TripPlanRequest();
        request.setCity("Rome");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tripPlannerService.generateTripPlan(request);
        });
        assertTrue(exception.getMessage().contains("AI trip planner is not configured"));
    }

    @Test
    void generateTripPlan_ApiCallFails() {
        // Arrange
        TripPlanRequest request = new TripPlanRequest();
        request.setCity("Rome");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenThrow(new RuntimeException("Connection timed out"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tripPlannerService.generateTripPlan(request);
        });
        assertTrue(exception.getMessage().contains("Gemini API call failed"));
    }

    @Test
    void generateTripPlan_InvalidResponse() {
        // Arrange
        TripPlanRequest request = new TripPlanRequest();
        request.setCity("Rome");

        // Return a response body with invalid candidates structure
        Map<String, Object> invalidResponseBody = Map.of(
                "candidates", List.of()
        );

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(invalidResponseBody);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            tripPlannerService.generateTripPlan(request);
        });
        assertTrue(exception.getMessage().contains("The trip planner returned an unexpected response"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateTripPlan_WithHotelContext() {
        // Arrange
        TripPlanRequest request = new TripPlanRequest();
        request.setCity("London");
        request.setHotelId(123L);

        HotelDto mockHotel = new HotelDto();
        mockHotel.setId(123L);
        mockHotel.setName("The Savoy");
        mockHotel.setCity("London");
        mockHotel.setAmenities(new String[]{"Spa", "Free WiFi"});
        HotelContactInfo contactInfo = new HotelContactInfo();
        contactInfo.setAddress("Strand, London WC2R 0EZ");
        mockHotel.setContactInfo(contactInfo);

        when(hotelService.getHotelById(123L)).thenReturn(mockHotel);

        String mockResponseJson = "{\"destination\": \"London\", \"summary\": \"Ok\", \"days\": [], \"tips\": []}";
        Map<String, Object> geminiResponseBody = Map.of(
                "candidates", List.of(Map.of(
                        "content", Map.of(
                                "parts", List.of(Map.of("text", mockResponseJson))
                        )
                ))
        );

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(geminiResponseBody);

        // Act
        tripPlannerService.generateTripPlan(request);

        // Assert
        verify(hotelService, times(1)).getHotelById(123L);
        
        ArgumentCaptor<Map<String, Object>> requestBodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestBodySpec).body(requestBodyCaptor.capture());
        
        Map<String, Object> capturedBody = requestBodyCaptor.getValue();
        List<Map<String, Object>> contents = (List<Map<String, Object>>) capturedBody.get("contents");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) contents.get(0).get("parts");
        String prompt = (String) parts.get(0).get("text");
        
        assertTrue(prompt.contains("The traveller is staying at this hotel"));
        assertTrue(prompt.contains("The Savoy"));
        assertTrue(prompt.contains("Strand, London"));
    }
}
