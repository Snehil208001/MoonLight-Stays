package com.snehil.moon_stays_androidapp.core.navigation

sealed class Screen(val route: String) {
    class Splash : Screen("splash")
    class Login : Screen("login")
    class SignUp : Screen("signup")
    class Onboarding : Screen("onboarding")
    class Dashboard : Screen("dashboard")
    class HotelDetail(val hotelId: Int) : Screen("hotel_detail")
    class TripPlanner : Screen("trip_planner")
}
