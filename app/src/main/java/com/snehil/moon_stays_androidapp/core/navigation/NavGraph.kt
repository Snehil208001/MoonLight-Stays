package com.snehil.moon_stays_androidapp.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.snehil.moon_stays_androidapp.mainui.loginscreen.ui.LoginScreen
import com.snehil.moon_stays_androidapp.mainui.loginscreen.viewmodel.LoginScreenViewModel
import com.snehil.moon_stays_androidapp.mainui.signupscreen.ui.SignUpScreen
import com.snehil.moon_stays_androidapp.mainui.signupscreen.viewmodel.SignUpScreenViewModel
import com.snehil.moon_stays_androidapp.mainui.splashscreen.ui.SplashScreen
import com.snehil.moon_stays_androidapp.mainui.splashscreen.viewmodel.SplashScreenViewModel
import com.snehil.moon_stays_androidapp.mainui.onboarding.ui.OnboardingScreen
import com.snehil.moon_stays_androidapp.mainui.dashboard.ui.DashboardScreen
import com.snehil.moon_stays_androidapp.mainui.dashboard.ui.ManagerDashboardScreen
import com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel.DashboardViewModel
import com.snehil.moon_stays_androidapp.mainui.hoteldetail.ui.HotelDetailScreen
import com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel.HotelDetailViewModel

@Composable
fun NavGraph(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    splashViewModel: SplashScreenViewModel,
    loginViewModel: LoginScreenViewModel,
    signUpViewModel: SignUpScreenViewModel,
    dashboardViewModel: DashboardViewModel,
    hotelDetailViewModel: HotelDetailViewModel,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    when (currentScreen) {
        // Flow mirrors the web app: Splash → Onboarding (first launch only) →
        // Login → Dashboard. A saved session skips straight to the Dashboard.
        is Screen.Splash -> {
            SplashScreen(
                viewModel = splashViewModel,
                onNavigateNext = {
                    when {
                        splashViewModel.isLoggedIn() -> {
                            dashboardViewModel.setManagerMode(splashViewModel.isManager())
                            onNavigate(Screen.Dashboard())
                        }
                        !splashViewModel.isOnboardingDone() -> onNavigate(Screen.Onboarding())
                        else -> onNavigate(Screen.Login())
                    }
                },
                modifier = modifier.padding(innerPadding)
            )
        }
        is Screen.Onboarding -> {
            OnboardingScreen(
                onComplete = {
                    splashViewModel.markOnboardingDone()
                    onNavigate(Screen.Login())
                },
                modifier = modifier.padding(innerPadding)
            )
        }
        is Screen.Login -> {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToSignUp = { onNavigate(Screen.SignUp()) },
                onLoginSuccess = {
                    // Role comes from the backend profile, saved during login
                    dashboardViewModel.setManagerMode(loginViewModel.isManagerAccount())
                    onNavigate(Screen.Dashboard())
                },
                modifier = modifier.padding(innerPadding)
            )
        }
        is Screen.SignUp -> {
            SignUpScreen(
                viewModel = signUpViewModel,
                onNavigateToLogin = { onNavigate(Screen.Login()) },
                modifier = modifier.padding(innerPadding)
            )
        }
        is Screen.Dashboard -> {
            val isManagerMode by dashboardViewModel.isManagerMode.collectAsState()
            if (isManagerMode) {
                ManagerDashboardScreen(
                    viewModel = dashboardViewModel,
                    onLogout = {
                        dashboardViewModel.logout()
                        onNavigate(Screen.Login())
                    },
                    modifier = modifier.padding(innerPadding)
                )
            } else {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToHotelDetail = { onNavigate(Screen.HotelDetail(it)) },
                    onLogout = {
                        dashboardViewModel.logout()
                        onNavigate(Screen.Login())
                    },
                    modifier = modifier.padding(innerPadding)
                )
            }
        }
        is Screen.HotelDetail -> {
            HotelDetailScreen(
                hotelId = currentScreen.hotelId,
                dashboardViewModel = dashboardViewModel,
                detailViewModel = hotelDetailViewModel,
                onNavigateBack = { onNavigate(Screen.Dashboard()) },
                modifier = modifier.padding(innerPadding)
            )
        }
    }
}
