package com.snehil.moon_stays_androidapp.data.remote.dto

/** Mirrors backend com.moonlight.project.airBnbApp.dto.TripPlanRequest */
data class TripPlanRequest(
    val city: String,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val numberOfGuests: Int? = null,
    val interests: List<String>? = null,
    val budgetLevel: String? = null,
    val hotelId: Long? = null
)

/** Mirrors backend com.moonlight.project.airBnbApp.dto.TripPlanResponse */
data class TripPlanResponse(
    val destination: String? = null,
    val summary: String? = null,
    val days: List<TripDayPlan>? = null,
    val tips: List<String>? = null
)

data class TripDayPlan(
    val day: Int? = null,
    val title: String? = null,
    val activities: List<TripActivity>? = null,
    val mealSuggestion: String? = null
)

data class TripActivity(
    val timeOfDay: String? = null,
    val title: String? = null,
    val description: String? = null
)
