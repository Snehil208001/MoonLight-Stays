package com.snehil.moon_stays_androidapp.mainui.signupscreen.ui

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snehil.moon_stays_androidapp.mainui.signupscreen.viewmodel.SignUpScreenViewModel
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurface
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurfaceVariant
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimary
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimaryFixedDim
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurface
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurfaceContainer
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SignUpScreen(
    viewModel: SignUpScreenViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fullName by viewModel.fullName.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val passwordVisible by viewModel.passwordVisible.collectAsState()
    val confirmPasswordVisible by viewModel.confirmPasswordVisible.collectAsState()
    val isHotelManager by viewModel.isHotelManager.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val signUpSuccess by viewModel.signUpSuccess.collectAsState()

    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            onNavigateToLogin()
            viewModel.resetSuccessState()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "signup_animations")

    // Shader time driver
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shader_time"
    )

    // Floating card vertical position driver
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (-8).dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MoonSurface)
    ) {
        // 1. Shifting Gradient Background (Mesh Shader approximation)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2

            // Shifting deep blue/nocturnal base glow
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

            // Shifting electric cyan highlights
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

        // Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MoonSurface.copy(alpha = 0.8f),
                            Color.Transparent,
                            MoonSurface
                        )
                    )
                )
        )

        // 2. Fixed Top Navigation (Header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0x1F121222))
                .border(width = 1.dp, color = Color(0x1AFFFFFF), shape = RoundedCornerShape(0.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MoonLight Stays",
                color = MoonPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        // 3. Scrollable Main Content containing the Signup Card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Floating Signup Card
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 500.dp)
                    .offset(y = floatOffset.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0x1AFFFFFF),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(
                        color = Color(0x0DFFFFFF),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(vertical = 32.dp, horizontal = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header Title & Subtitle inside card
                    Text(
                        text = "MoonLight Stays",
                        color = MoonPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Join the next generation of celestial travel.",
                        color = MoonOnSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Form Fields
                    // Full Name Field
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Full Name",
                            color = MoonOnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = viewModel::onFullNameChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "John Doe",
                                    color = MoonOnSurfaceVariant.copy(alpha = 0.4f),
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Person Icon",
                                    tint = MoonOnSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MoonPrimaryFixedDim,
                                unfocusedBorderColor = Color(0x1AFFFFFF),
                                focusedContainerColor = Color(0x0DFFFFFF),
                                unfocusedContainerColor = Color(0x08FFFFFF),
                                focusedTextColor = MoonPrimary,
                                unfocusedTextColor = MoonPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Email Field
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Email Address",
                            color = MoonOnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = viewModel::onEmailChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "john@moonlight.com",
                                    color = MoonOnSurfaceVariant.copy(alpha = 0.4f),
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = MoonOnSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MoonPrimaryFixedDim,
                                unfocusedBorderColor = Color(0x1AFFFFFF),
                                focusedContainerColor = Color(0x0DFFFFFF),
                                unfocusedContainerColor = Color(0x08FFFFFF),
                                focusedTextColor = MoonPrimary,
                                unfocusedTextColor = MoonPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Password Fields Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Password Field
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Password",
                                color = MoonOnSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = viewModel::onPasswordChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "••••••••",
                                        color = MoonOnSurfaceVariant.copy(alpha = 0.4f),
                                        fontSize = 14.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock Icon",
                                        tint = MoonOnSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = viewModel::togglePasswordVisibility) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Visibility",
                                            tint = MoonOnSurfaceVariant
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MoonPrimaryFixedDim,
                                    unfocusedBorderColor = Color(0x1AFFFFFF),
                                    focusedContainerColor = Color(0x0DFFFFFF),
                                    unfocusedContainerColor = Color(0x08FFFFFF),
                                    focusedTextColor = MoonPrimary,
                                    unfocusedTextColor = MoonPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Confirm Password Field
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Confirm",
                                color = MoonOnSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = viewModel::onConfirmPasswordChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "••••••••",
                                        color = MoonOnSurfaceVariant.copy(alpha = 0.4f),
                                        fontSize = 14.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Shield Icon",
                                        tint = MoonOnSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = viewModel::toggleConfirmPasswordVisibility) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Visibility",
                                            tint = MoonOnSurfaceVariant
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MoonPrimaryFixedDim,
                                    unfocusedBorderColor = Color(0x1AFFFFFF),
                                    focusedContainerColor = Color(0x0DFFFFFF),
                                    unfocusedContainerColor = Color(0x08FFFFFF),
                                    focusedTextColor = MoonPrimary,
                                    unfocusedTextColor = MoonPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Role Toggle for Hotel Manager
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x08FFFFFF), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CorporateFare,
                                contentDescription = "Corporate Fare Icon",
                                tint = MoonPrimaryFixedDim,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Hotel Manager",
                                    color = MoonOnSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Switch to business account",
                                    color = MoonOnSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Switch(
                            checked = isHotelManager,
                            onCheckedChange = viewModel::toggleHotelManager,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MoonPrimaryFixedDim,
                                uncheckedThumbColor = MoonOnSurfaceVariant,
                                uncheckedTrackColor = MoonSurfaceContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Create Account Button
                    Button(
                        onClick = viewModel::signUp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(
                                width = 1.5.dp,
                                color = MoonPrimaryFixedDim,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MoonPrimaryFixedDim.copy(alpha = 0.1f),
                            contentColor = MoonPrimaryFixedDim
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword == password
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MoonPrimaryFixedDim,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Create Account",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Arrow Forward",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // OR CONTINUE WITH Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0x1AFFFFFF))
                        )
                        Text(
                            text = "OR CONTINUE WITH",
                            color = MoonOnSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0x1AFFFFFF))
                        )
                    }

                    // Social Buttons (Mock / UI only to match Stitch specs)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { /* Google Action */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0x1AFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x08FFFFFF),
                                contentColor = MoonOnSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Google", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = { /* GitHub Action */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0x1AFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x08FFFFFF),
                                contentColor = MoonOnSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("GitHub", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Already have account link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = MoonOnSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Log in here",
                            color = MoonPrimaryFixedDim,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .clickable { onNavigateToLogin() }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // System Status Bar (ATS Style/Diagnostic) at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF00E479), CircleShape)
                    )
                    Text(
                        text = "SECURE AUTH NODE: 0x44F",
                        color = MoonOnSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ENCRYPTION: AES-256",
                        color = MoonOnSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified User",
                        tint = MoonOnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
