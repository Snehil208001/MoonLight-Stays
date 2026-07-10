package com.moonlight.project.airBnbApp.controller;

import com.moonlight.project.airBnbApp.dto.TripPlanRequest;
import com.moonlight.project.airBnbApp.dto.TripPlanResponse;
import com.moonlight.project.airBnbApp.service.TripPlannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class TripPlannerController {

    private final TripPlannerService tripPlannerService;

    @PostMapping("/trip-plan")
    public ResponseEntity<TripPlanResponse> generateTripPlan(@Valid @RequestBody TripPlanRequest request) {
        return ResponseEntity.ok(tripPlannerService.generateTripPlan(request));
    }
}
