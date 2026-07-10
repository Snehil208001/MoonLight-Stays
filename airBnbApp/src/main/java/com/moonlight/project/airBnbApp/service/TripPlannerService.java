package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.TripPlanRequest;
import com.moonlight.project.airBnbApp.dto.TripPlanResponse;

public interface TripPlannerService {

    TripPlanResponse generateTripPlan(TripPlanRequest request);
}
