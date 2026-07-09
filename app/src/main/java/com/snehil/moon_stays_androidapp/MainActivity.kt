package com.snehil.moon_stays_androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.snehil.moon_stays_androidapp.core.navigation.NavGraph
import com.snehil.moon_stays_androidapp.core.navigation.Screen
import com.snehil.moon_stays_androidapp.mainui.loginscreen.viewmodel.LoginScreenViewModel
import com.snehil.moon_stays_androidapp.mainui.signupscreen.viewmodel.SignUpScreenViewModel
import com.snehil.moon_stays_androidapp.mainui.splashscreen.viewmodel.SplashScreenViewModel
import com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel.DashboardViewModel
import com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel.HotelDetailViewModel
import com.snehil.moon_stays_androidapp.ui.theme.MoonStaysAndroidAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashScreenViewModel by viewModels()
    private val loginViewModel: LoginScreenViewModel by viewModels()
    private val signUpViewModel: SignUpScreenViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val hotelDetailViewModel: HotelDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoonStaysAndroidAppTheme {
                var screenStack by remember { mutableStateOf(listOf<Screen>(Screen.Splash())) }
                val currentScreen = screenStack.last()

                // Intercept system back press when stack has history
                androidx.activity.compose.BackHandler(enabled = screenStack.size > 1) {
                    screenStack = screenStack.dropLast(1)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        currentScreen = currentScreen,
                        onNavigate = { nextScreen ->
                            screenStack = when (nextScreen) {
                                is Screen.Dashboard -> listOf(nextScreen) // clear stack on root screens
                                is Screen.Login -> listOf(nextScreen)
                                is Screen.Splash -> listOf(nextScreen)
                                else -> screenStack + nextScreen
                            }
                        },
                        splashViewModel = splashViewModel,
                        loginViewModel = loginViewModel,
                        signUpViewModel = signUpViewModel,
                        dashboardViewModel = dashboardViewModel,
                        hotelDetailViewModel = hotelDetailViewModel,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}