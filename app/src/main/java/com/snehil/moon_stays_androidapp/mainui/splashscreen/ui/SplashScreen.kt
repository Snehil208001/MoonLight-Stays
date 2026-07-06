package com.snehil.moon_stays_androidapp.mainui.splashscreen.ui

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snehil.moon_stays_androidapp.mainui.splashscreen.viewmodel.SplashScreenViewModel
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurfaceVariant
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimary
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimaryFixedDim
import com.snehil.moon_stays_androidapp.ui.theme.MoonSecondary
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurface
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurfaceContainer
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    viewModel: SplashScreenViewModel,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFinished by viewModel.isFinished.collectAsState()
    val statusText by viewModel.statusText.collectAsState()

    LaunchedEffect(isFinished) {
        if (isFinished) {
            onNavigateNext()
        }
    }

    var animateCard by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateCard = true
    }

    val cardAlpha by animateFloatAsState(
        targetValue = if (animateCard) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = EaseInOut),
        label = "card_alpha"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (animateCard) 1f else 0.95f,
        animationSpec = tween(durationMillis = 1200, easing = EaseInOut),
        label = "card_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "shader_effects")

    // Shader time driver
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shader_time"
    )

    // Indeterminate loader position driver
    val loaderProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loader_progress"
    )

    // Pulsing glow size driver for moon icon
    val logoGlowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_glow_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MoonSurface)
    ) {
        // 1. WebGL Shader approximation background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2

            // Base mesh background gradients
            // Moving deep blue/indigo gradient
            val blueGlowX = centerX + sin(time) * 150.dp.toPx()
            val blueGlowY = centerY + cos(time * 0.8f) * 200.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0F0F23), Color.Transparent),
                    center = Offset(blueGlowX, blueGlowY),
                    radius = 450.dp.toPx()
                ),
                center = Offset(blueGlowX, blueGlowY),
                radius = 450.dp.toPx()
            )

            // Moving deep red/orange gradient
            val redGlowX = centerX - cos(time * 0.7f) * 180.dp.toPx()
            val redGlowY = centerY - sin(time * 0.9f) * 150.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2E1515), Color.Transparent),
                    center = Offset(redGlowX, redGlowY),
                    radius = 350.dp.toPx()
                ),
                center = Offset(redGlowX, redGlowY),
                radius = 350.dp.toPx()
            )

            // Primary Electric Cyan wave highlights
            val cyanGlowX = centerX + sin(time * 1.2f) * 200.dp.toPx()
            val cyanGlowY = centerY - cos(time * 0.9f) * 250.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MoonPrimaryFixedDim.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(cyanGlowX, cyanGlowY),
                    radius = 300.dp.toPx()
                ),
                center = Offset(cyanGlowX, cyanGlowY),
                radius = 300.dp.toPx()
            )
        }

        // Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x99000000)
                        )
                    )
                )
        )

        // 2. Central Glassmorphic Card
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(cardAlpha)
                .scale(cardScale)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0x1AFFFFFF), // glass-border: rgba(255,255,255,0.1)
                        shape = RoundedCornerShape(32.dp)
                    )
                    .background(
                        color = Color(0x0DFFFFFF), // glass-fill: rgba(255,255,255,0.05)
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(vertical = 40.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo/Icon with glow
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Blurred cyan glow behind the icon
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(logoGlowScale)
                        ) {
                            val path = Path().apply {
                                addOval(Rect(Offset.Zero, size))
                            }
                            val cutPath = Path().apply {
                                addOval(
                                    Rect(
                                        Offset(x = size.width * 0.28f, y = -size.height * 0.05f),
                                        size * 0.95f
                                    )
                                )
                            }
                            val moonPath = Path.combine(PathOperation.Difference, path, cutPath)
                            drawPath(
                                path = moonPath,
                                color = MoonPrimaryFixedDim.copy(alpha = 0.35f)
                            )
                        }

                        // Sharp solid cyan moon
                        Canvas(modifier = Modifier.size(48.dp)) {
                            val path = Path().apply {
                                addOval(Rect(Offset.Zero, size))
                            }
                            val cutPath = Path().apply {
                                addOval(
                                    Rect(
                                        Offset(x = size.width * 0.28f, y = -size.height * 0.05f),
                                        size * 0.95f
                                    )
                                )
                            }
                            val moonPath = Path.combine(PathOperation.Difference, path, cutPath)
                            drawPath(
                                path = moonPath,
                                color = MoonPrimaryFixedDim
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Brand Title (Electric Cyan Glow text)
                    Box(contentAlignment = Alignment.Center) {
                        // Glow duplicate
                        Text(
                            text = "MoonLight Stays",
                            color = MoonPrimaryFixedDim.copy(alpha = 0.4f),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-1).sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(y = 1.dp)
                        )
                        // Foreground
                        Text(
                            text = "MoonLight Stays",
                            color = MoonPrimaryFixedDim,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-1).sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tagline
                    Text(
                        text = "Welcome to Nocturnal Sanctuary",
                        color = MoonOnSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 3. Footer / Loading Area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indeterminate Custom sliding loading bar
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x1AFFFFFF)) // rgba(255,255,255,0.1)
            ) {
                // Sliding bar fill
                val barWidth = 60.dp
                val maxOffset = 160.dp - barWidth
                val slideX = maxOffset * loaderProgress
                
                Box(
                    modifier = Modifier
                        .offset(x = slideX)
                        .width(barWidth)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MoonPrimaryFixedDim.copy(alpha = 0.2f),
                                    MoonPrimaryFixedDim,
                                    MoonPrimaryFixedDim.copy(alpha = 0.2f)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Text
            Text(
                text = statusText,
                color = Color(0xFF839493).copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        }
    }
}
