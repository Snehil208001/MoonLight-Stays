package com.snehil.moon_stays_androidapp.mainui.tripplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snehil.moon_stays_androidapp.mainui.tripplanner.viewmodel.TripPlannerViewModel
import com.snehil.moon_stays_androidapp.ui.theme.*

private val INTEREST_OPTIONS = listOf(
    "Food & Dining", "History & Culture", "Nature & Outdoors", "Nightlife",
    "Shopping", "Art & Museums", "Adventure", "Relaxation"
)
private val BUDGET_OPTIONS = listOf("BUDGET", "MODERATE", "LUXURY")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TripPlannerScreen(
    viewModel: TripPlannerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    prefillCity: String = "",
    prefillHotelId: Long? = null
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val tripPlan by viewModel.tripPlan.collectAsState()

    var city by remember { mutableStateOf(prefillCity) }
    var checkIn by remember { mutableStateOf("") }
    var checkOut by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf("2") }
    var budget by remember { mutableStateOf("MODERATE") }
    val interests = remember { mutableStateListOf<String>() }
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MoonSurface,
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(MoonSurface.copy(alpha = 0.9f))
                    .border(1.dp, GlassBorder)
                    .padding(horizontal = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MoonPrimary
                    )
                }
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan)
                Text(
                    "AI Trip Planner",
                    color = MoonPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                "Tell us where you're headed and we'll craft a day-by-day itinerary.",
                color = TextSecondary,
                fontSize = 14.sp
            )

            GlassField(value = city, onValueChange = { city = it }, label = "Destination city", placeholder = "e.g. Jaipur")

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                GlassDateField(
                    value = checkIn,
                    label = "Check-in",
                    onClick = { showCheckInPicker = true },
                    modifier = Modifier.weight(1f)
                )
                GlassDateField(
                    value = checkOut,
                    label = "Check-out",
                    onClick = { showCheckOutPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            GlassField(
                value = guests,
                onValueChange = { input -> guests = input.filter { it.isDigit() }.take(2) },
                label = "Travellers",
                placeholder = "2",
                keyboardType = KeyboardType.Number
            )

            Text("Budget", color = TextSecondary, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                BUDGET_OPTIONS.forEach { option ->
                    val selected = budget == option
                    FilterChip(
                        selected = selected,
                        onClick = { budget = option },
                        label = { Text(option.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = GlassSurface,
                            labelColor = TextSecondary,
                            selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                            selectedLabelColor = AccentCyan
                        )
                    )
                }
            }

            Text("Interests", color = TextSecondary, fontSize = 13.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                INTEREST_OPTIONS.forEach { option ->
                    val selected = interests.contains(option)
                    FilterChip(
                        selected = selected,
                        onClick = { if (selected) interests.remove(option) else interests.add(option) },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = GlassSurface,
                            labelColor = TextSecondary,
                            selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                            selectedLabelColor = AccentCyan
                        )
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.generateTripPlan(
                        city = city,
                        checkInDate = checkIn,
                        checkOutDate = checkOut,
                        numberOfGuests = guests.toIntOrNull() ?: 1,
                        interests = interests.toList(),
                        budgetLevel = budget,
                        hotelId = prefillHotelId
                    )
                },
                enabled = !isLoading && city.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan.copy(alpha = 0.2f),
                    contentColor = AccentCyan,
                    disabledContainerColor = GlassSurface,
                    disabledContentColor = TextDisabled
                )
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.xs))
                Text(if (isLoading) "Planning your trip..." else "Generate Itinerary")
            }

            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(Spacing.lg), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentCyan)
                }
            }

            errorMessage?.let { msg ->
                GlassCard {
                    Text(msg, color = ErrorRed, fontSize = 14.sp)
                }
            }

            tripPlan?.let { plan ->
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                        Text(plan.destination ?: city, color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    plan.summary?.let {
                        Spacer(Modifier.height(Spacing.xs))
                        Text(it, color = TextSecondary, fontSize = 14.sp)
                    }
                }

                plan.days?.forEach { day ->
                    GlassCard {
                        Text(
                            "Day ${day.day}: ${day.title ?: ""}",
                            color = AccentCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        day.activities?.forEach { act ->
                            Row(Modifier.padding(vertical = Spacing.xxs), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Box(
                                    Modifier
                                        .background(AccentCoral.copy(alpha = 0.2f), RoundedCornerShape(Radii.sm))
                                        .border(1.dp, AccentCoral.copy(alpha = 0.4f), RoundedCornerShape(Radii.sm))
                                        .padding(horizontal = Spacing.xs, vertical = 2.dp)
                                ) {
                                    Text(act.timeOfDay ?: "", color = AccentCoral, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                                Column {
                                    Text(act.title ?: "", color = MoonPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(act.description ?: "", color = TextMuted, fontSize = 13.sp)
                                }
                            }
                        }
                        day.mealSuggestion?.let {
                            Spacer(Modifier.height(Spacing.xs))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                Text(it, color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                val tips = plan.tips
                if (!tips.isNullOrEmpty()) {
                    GlassCard {
                        Text("Travel Tips", color = MoonPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(Spacing.xs))
                        tips.forEach { tip ->
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Text("•", color = AccentCyan, fontSize = 14.sp)
                                Text(tip, color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))
        }
    }

    // Check-in date picker (today onwards)
    if (showCheckInPicker) {
        MoonDatePickerDialog(
            initialDateIso = checkIn,
            minDateIso = java.time.LocalDate.now().toString(),
            onDateSelected = { selected ->
                checkIn = selected
                // Keep check-out strictly after check-in when one is already set
                val ci = try { java.time.LocalDate.parse(selected) } catch (e: Exception) { null }
                val co = try { java.time.LocalDate.parse(checkOut) } catch (e: Exception) { null }
                if (ci != null && co != null && !co.isAfter(ci)) {
                    checkOut = ci.plusDays(1).toString()
                }
            },
            onDismiss = { showCheckInPicker = false }
        )
    }

    // Check-out date picker (must be after check-in)
    if (showCheckOutPicker) {
        val minCheckOut = try {
            java.time.LocalDate.parse(checkIn).plusDays(1).toString()
        } catch (e: Exception) {
            java.time.LocalDate.now().plusDays(1).toString()
        }
        MoonDatePickerDialog(
            initialDateIso = if (checkOut >= minCheckOut) checkOut else minCheckOut,
            minDateIso = minCheckOut,
            onDateSelected = { checkOut = it },
            onDismiss = { showCheckOutPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radii.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MoonPrimary,
            unfocusedTextColor = MoonPrimary,
            focusedBorderColor = AccentCyan.copy(alpha = 0.5f),
            unfocusedBorderColor = GlassBorder,
            focusedLabelColor = AccentCyan,
            unfocusedLabelColor = TextMuted,
            cursorColor = AccentCyan,
            focusedContainerColor = GlassSurface,
            unfocusedContainerColor = GlassSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassDateField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            label = { Text(label) },
            placeholder = { Text("YYYY-MM-DD") },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = TextMuted) },
            singleLine = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Radii.md),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MoonPrimary,
                disabledBorderColor = GlassBorder,
                disabledLabelColor = TextMuted,
                disabledPlaceholderColor = TextMuted,
                disabledLeadingIconColor = TextMuted,
                disabledContainerColor = GlassSurface
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(Radii.md))
                .clickable(onClick = onClick)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoonDatePickerDialog(
    initialDateIso: String,
    minDateIso: String?,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    fun isoToUtcMillis(iso: String): Long? = try {
        java.time.LocalDate.parse(iso)
            .atStartOfDay(java.time.ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    } catch (e: Exception) {
        null
    }

    val minMillis = remember(minDateIso) { minDateIso?.let { isoToUtcMillis(it) } }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = isoToUtcMillis(initialDateIso)
            ?: System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return minMillis == null || utcTimeMillis >= minMillis
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val iso = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneOffset.UTC)
                        .toLocalDate()
                        .toString()
                    onDateSelected(iso)
                }
                onDismiss()
            }) { Text("OK", color = AccentCyan, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassSurface, RoundedCornerShape(Radii.lg))
            .border(1.dp, GlassBorder, RoundedCornerShape(Radii.lg))
            .padding(Spacing.md),
        content = content
    )
}
