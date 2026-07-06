package com.snehil.moon_stays_androidapp.mainui.onboarding.ui

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurfaceVariant
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimary
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimaryFixedDim
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurface
import kotlin.math.cos
import kotlin.math.sin

data class OnboardingStep(
    val title: String,
    val subtext: String,
    val isHighlight: Boolean = false
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(0) }
    val steps = listOf(
        OnboardingStep(
            title = "Discover the Ethereal",
            subtext = "Find breathtaking hotels curated for your vibe."
        ),
        OnboardingStep(
            title = "Smart Dynamic Pricing",
            subtext = "Our engine tracks urgency, holidays, and surges to give you transparent rates.",
            isHighlight = true
        ),
        OnboardingStep(
            title = "Seamless Escapes",
            subtext = "Book instantly and securely. Your getaway awaits."
        )
    )
    val isLast = step == steps.size - 1

    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_animations")

    // Background gradient shift driver
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shader_time"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MoonSurface)
    ) {
        // Shifting Gradient Background (Mesh Shader approximation)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2

            val blueGlowX = centerX + sin(time) * 120.dp.toPx()
            val blueGlowY = centerY + cos(time * 0.8f) * 180.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0F0F23), Color.Transparent),
                    center = Offset(blueGlowX, blueGlowY),
                    radius = 400.dp.toPx()
                ),
                center = Offset(blueGlowX, blueGlowY),
                radius = 400.dp.toPx()
            )

            val cyanGlowX = centerX - cos(time * 0.9f) * 200.dp.toPx()
            val cyanGlowY = centerY - sin(time * 1.1f) * 220.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MoonPrimaryFixedDim.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(cyanGlowX, cyanGlowY),
                    radius = 280.dp.toPx()
                ),
                center = Offset(cyanGlowX, cyanGlowY),
                radius = 280.dp.toPx()
            )
        }

        // Skip Button
        Text(
            text = "Skip to Search",
            color = MoonOnSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 24.dp)
                .clickable { onComplete() }
        )

        // Carousel Card Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0x1AFFFFFF),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(
                        color = Color(0x0DFFFFFF),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0x14FFFFFF), CircleShape)
                            .border(1.dp, Color(0x1AFFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Sparkles Icon",
                            tint = MoonPrimaryFixedDim,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = steps[step].title,
                        color = MoonPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = steps[step].subtext,
                        color = if (steps[step].isHighlight) MoonPrimaryFixedDim else MoonOnSurfaceVariant,
                        fontSize = 16.sp,
                        fontWeight = if (steps[step].isHighlight) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Navigation Row (Indicators + Button)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Dots Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.forEachIndexed { index, _ ->
                        val isActive = index == step
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isActive) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isActive) MoonPrimaryFixedDim else Color(0x4DFFFFFF))
                        )
                    }
                }

                // Action Button
                Button(
                    onClick = {
                        if (isLast) onComplete() else step++
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x1AFFFFFF),
                        contentColor = MoonPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLast) "Get Started" else "Next",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Arrow Icon",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
