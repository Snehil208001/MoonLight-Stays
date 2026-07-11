package com.moonlight.project.airBnbApp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moonlight.project.airBnbApp.dto.TripPlanRequest;
import com.moonlight.project.airBnbApp.dto.TripPlanResponse;
import com.moonlight.project.airBnbApp.repository.UserRepository;
import com.moonlight.project.airBnbApp.security.JWTService;
import com.moonlight.project.airBnbApp.service.TripPlannerService;
import com.moonlight.project.airBnbApp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TripPlannerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TripPlannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripPlannerService tripPlannerService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void generateTripPlan_Success() throws Exception {
        // Arrange
        TripPlanRequest request = new TripPlanRequest();
        request.setCity("Paris");
        request.setCheckInDate(LocalDate.now());
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        TripPlanResponse response = new TripPlanResponse();
        response.setDestination("Paris");
        response.setSummary("A beautiful 3-day trip in Paris");
        response.setDays(List.of());
        response.setTips(List.of("Tip 1", "Tip 2"));

        when(tripPlannerService.generateTripPlan(any(TripPlanRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/ai/trip-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.destination").value("Paris"))
                .andExpect(jsonPath("$.data.summary").value("A beautiful 3-day trip in Paris"))
                .andExpect(jsonPath("$.data.tips[0]").value("Tip 1"));
    }

    @Test
    void generateTripPlan_ValidationFailed_MissingCity() throws Exception {
        // Arrange
        TripPlanRequest request = new TripPlanRequest();
        // city is not set, which is @NotBlank

        // Act & Assert
        mockMvc.perform(post("/ai/trip-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
