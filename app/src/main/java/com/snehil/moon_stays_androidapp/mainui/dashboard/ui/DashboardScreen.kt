package com.snehil.moon_stays_androidapp.mainui.dashboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snehil.moon_stays_androidapp.core.util.formatPrice
import com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel.BookingDto
import com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel.DashboardViewModel
import com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel.HotelPriceDto
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurface
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurfaceVariant
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimary
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimaryFixedDim
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurface
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurfaceContainer
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurfaceContainerHighest

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToHotelDetail: (Int) -> Unit,
    onNavigateToTripPlanner: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Explore") }

    // Intercept back press to return to the Explore tab if on another tab
    androidx.activity.compose.BackHandler(enabled = activeTab != "Explore") {
        activeTab = "Explore"
    }



    Scaffold(
        topBar = {
            DashboardTopBar(activeTab = activeTab, onTripPlannerClick = onNavigateToTripPlanner)
        },
        bottomBar = {
            DashboardBottomNavBar(
                activeTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(MoonSurface),
        containerColor = MoonSurface
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Explore" -> ExploreTabContent(viewModel, onNavigateToHotelDetail)
                "Favorites" -> FavoritesTabContent(viewModel, onNavigateToHotelDetail)
                "Bookings" -> BookingsTabContent(viewModel)
                "Profile" -> ProfileTabContent(viewModel, onLogout)
            }
        }
    }
}

@Composable
fun DashboardTopBar(activeTab: String, onTripPlannerClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MoonSurface.copy(alpha = 0.9f))
            .border(width = 1.dp, color = Color(0x1AFFFFFF))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x14FFFFFF), CircleShape)
                    .border(1.dp, Color(0x1AFFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TravelExplore,
                    contentDescription = "App Icon",
                    tint = MoonPrimaryFixedDim,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "MoonLight Stays",
                color = MoonPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onTripPlannerClick)
                    .background(Color(0x14FFFFFF), CircleShape)
                    .border(1.dp, Color(0x1AFFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Trip Planner",
                    tint = MoonPrimaryFixedDim,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = activeTab.uppercase(),
                    color = MoonPrimaryFixedDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun ExploreTabContent(
    viewModel: DashboardViewModel,
    onNavigateToHotelDetail: (Int) -> Unit
) {
    val city by viewModel.city.collectAsState()
    val checkInDate by viewModel.checkInDate.collectAsState()
    val checkOutDate by viewModel.checkOutDate.collectAsState()
    val roomsCount by viewModel.roomsCount.collectAsState()
    val selectedRoomType by viewModel.selectedRoomType.collectAsState()
    val promoCode by viewModel.promoCode.collectAsState()
    val promoDiscount by viewModel.promoDiscount.collectAsState()
    val promoError by viewModel.promoError.collectAsState()
    val isPromoLoading by viewModel.isPromoLoading.collectAsState()
    val hotels by viewModel.hotels.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var promoInput by remember { mutableStateOf("") }

    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }

    // Advanced search filter inputs (mirrors web "More filters")
    var showFilters by remember { mutableStateOf(false) }
    var minPriceInput by remember { mutableStateOf("") }
    var maxPriceInput by remember { mutableStateOf("") }
    var roomTypeInput by remember { mutableStateOf("") }
    var amenityInput by remember { mutableStateOf("") }
    val hasActiveFilters = minPriceInput.isNotBlank() || maxPriceInput.isNotBlank() ||
        roomTypeInput.isNotBlank() || amenityInput.isNotBlank()
    val runSearch = {
        viewModel.applyFilters(
            minPriceInput.toIntOrNull(),
            maxPriceInput.toIntOrNull(),
            roomTypeInput,
            amenityInput
        )
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.triggerSearch() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Welcome Header
            Text(
                text = "Find Your Ethereal Escape",
                color = MoonPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Discover breathtaking hotels with transparent pricing",
                color = MoonOnSurfaceVariant,
                fontSize = 14.sp
            )
        }

        // Search booking form (MakeMyTrip logic)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Location Input
                    OutlinedTextField(
                        value = city,
                        onValueChange = viewModel::onCityChanged,
                        label = { Text("Destination City", color = MoonOnSurfaceVariant) },
                        placeholder = { Text("e.g. Neo-Tokyo", color = MoonOnSurfaceVariant.copy(0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Place, null, tint = MoonOnSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MoonPrimaryFixedDim,
                            unfocusedBorderColor = Color(0x1AFFFFFF),
                            focusedTextColor = MoonPrimary,
                            unfocusedTextColor = MoonPrimary
                        )
                    )

                    // Dates Grid — tapping a field opens a calendar picker
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = checkInDate,
                                onValueChange = { },
                                label = { Text("Check-In", color = MoonOnSurfaceVariant) },
                                leadingIcon = { Icon(Icons.Default.DateRange, null, tint = MoonOnSurfaceVariant) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0x1AFFFFFF),
                                    disabledTextColor = MoonPrimary,
                                    disabledLabelColor = MoonOnSurfaceVariant,
                                    disabledLeadingIconColor = MoonOnSurfaceVariant
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { showCheckInPicker = true }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = checkOutDate,
                                onValueChange = { },
                                label = { Text("Check-Out", color = MoonOnSurfaceVariant) },
                                leadingIcon = { Icon(Icons.Default.DateRange, null, tint = MoonOnSurfaceVariant) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0x1AFFFFFF),
                                    disabledTextColor = MoonPrimary,
                                    disabledLabelColor = MoonOnSurfaceVariant,
                                    disabledLeadingIconColor = MoonOnSurfaceVariant
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { showCheckOutPicker = true }
                            )
                        }
                    }

                    // Room & Guest Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rooms / Travelers", color = MoonOnSurfaceVariant, fontSize = 14.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { if (roomsCount > 1) viewModel.onRoomsCountChanged(roomsCount - 1) },
                                modifier = Modifier.size(36.dp).background(Color(0x14FFFFFF), CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, null, tint = MoonPrimary, modifier = Modifier.size(16.dp))
                            }
                            Text("$roomsCount", color = MoonPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(
                                onClick = { viewModel.onRoomsCountChanged(roomsCount + 1) },
                                modifier = Modifier.size(36.dp).background(Color(0x14FFFFFF), CircleShape)
                            ) {
                                Icon(Icons.Default.Add, null, tint = MoonPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // More filters toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.clickable { showFilters = !showFilters }
                    ) {
                        Icon(
                            imageVector = if (showFilters) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MoonOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("More filters", color = MoonOnSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        if (hasActiveFilters) {
                            Box(
                                modifier = Modifier
                                    .background(MoonPrimaryFixedDim.copy(0.2f), RoundedCornerShape(20.dp))
                                    .padding(vertical = 2.dp, horizontal = 8.dp)
                            ) {
                                Text("Active", color = MoonPrimaryFixedDim, fontSize = 10.sp)
                            }
                        }
                    }

                    if (showFilters) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SearchFilterField(
                                    value = minPriceInput,
                                    onValueChange = { minPriceInput = it.filter { c -> c.isDigit() } },
                                    label = "Min price (₹)",
                                    modifier = Modifier.weight(1f),
                                    numeric = true
                                )
                                SearchFilterField(
                                    value = maxPriceInput,
                                    onValueChange = { maxPriceInput = it.filter { c -> c.isDigit() } },
                                    label = "Max price (₹)",
                                    modifier = Modifier.weight(1f),
                                    numeric = true
                                )
                            }
                            SearchFilterField(
                                value = roomTypeInput,
                                onValueChange = { roomTypeInput = it },
                                label = "Room type (e.g. Deluxe)",
                                modifier = Modifier.fillMaxWidth()
                            )
                            SearchFilterField(
                                value = amenityInput,
                                onValueChange = { amenityInput = it },
                                label = "Amenity (e.g. Pool, WiFi)",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Search Button
                    Button(
                        onClick = { runSearch() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MoonPrimaryFixedDim.copy(0.15f),
                            contentColor = MoonPrimaryFixedDim
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = borderStroke(1.5.dp, MoonPrimaryFixedDim)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MoonPrimaryFixedDim, strokeWidth = 2.dp)
                        } else {
                            Text("Search Hotels", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Promo Codes segment
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Promo Codes", color = MoonPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = promoInput,
                        onValueChange = { promoInput = it },
                        placeholder = { Text("Enter Code (e.g. LUNAR25)", color = MoonOnSurfaceVariant.copy(0.4f), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MoonPrimaryFixedDim,
                            unfocusedBorderColor = Color(0x1AFFFFFF),
                            focusedTextColor = MoonPrimary,
                            unfocusedTextColor = MoonPrimary
                        )
                    )
                    Button(
                        onClick = { viewModel.applyPromoCode(promoInput) },
                        enabled = promoInput.isNotBlank() && !isPromoLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x14FFFFFF)),
                        modifier = Modifier.height(46.dp)
                    ) {
                        if (isPromoLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MoonPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Apply", color = MoonPrimary)
                        }
                    }
                }
                if (promoDiscount != null) {
                    Text(
                        text = "Promo applied: $promoDiscount% off base prices!",
                        color = Color(0xFF00E479),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (promoError != null) {
                    Text(
                        text = promoError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Hotels Grid List
        if (hotels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hotels found matching criteria.", color = MoonOnSurfaceVariant)
                }
            }
        } else {
            items(hotels) { hotelPrice ->
                HotelCard(
                    hotelPrice = hotelPrice,
                    isFavorite = favoriteIds.contains(hotelPrice.hotel.id),
                    onToggleFavorite = { viewModel.toggleFavorite(hotelPrice.hotel.id) },
                    onClick = { onNavigateToHotelDetail(hotelPrice.hotel.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    }

    // Check-In date picker
    if (showCheckInPicker) {
        MoonDatePickerDialog(
            initialDateIso = checkInDate,
            minDateIso = java.time.LocalDate.now().toString(),
            onDateSelected = { selected ->
                // Keep check-out strictly after the new check-in
                val newCheckOut = try {
                    val ci = java.time.LocalDate.parse(selected)
                    val co = java.time.LocalDate.parse(checkOutDate)
                    if (!co.isAfter(ci)) ci.plusDays(1).toString() else checkOutDate
                } catch (e: Exception) {
                    selected
                }
                viewModel.onDatesChanged(selected, newCheckOut)
                viewModel.triggerSearch()
            },
            onDismiss = { showCheckInPicker = false }
        )
    }

    // Check-Out date picker (must be after check-in)
    if (showCheckOutPicker) {
        val minCheckOut = try {
            java.time.LocalDate.parse(checkInDate).plusDays(1).toString()
        } catch (e: Exception) {
            java.time.LocalDate.now().plusDays(1).toString()
        }
        MoonDatePickerDialog(
            initialDateIso = if (checkOutDate >= minCheckOut) checkOutDate else minCheckOut,
            minDateIso = minCheckOut,
            onDateSelected = { selected ->
                viewModel.onDatesChanged(checkInDate, selected)
                viewModel.triggerSearch()
            },
            onDismiss = { showCheckOutPicker = false }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
            }) { Text("OK", color = MoonPrimaryFixedDim, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MoonOnSurfaceVariant) }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun SearchFilterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    numeric: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MoonOnSurfaceVariant, fontSize = 12.sp) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = if (numeric) androidx.compose.ui.text.input.KeyboardType.Number
            else androidx.compose.ui.text.input.KeyboardType.Text
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MoonPrimaryFixedDim,
            unfocusedBorderColor = Color(0x1AFFFFFF),
            focusedTextColor = MoonPrimary,
            unfocusedTextColor = MoonPrimary
        )
    )
}

@Composable
fun HotelCard(
    hotelPrice: HotelPriceDto,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column {
            // Image with name/city overlaid at the bottom (matches web HotelCard)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp)
            ) {
                val imageUrl = hotelPrice.hotel.photos.firstOrNull()
                if (imageUrl.isNullOrEmpty()) {
                    HotelImagePlaceholder(name = hotelPrice.hotel.name)
                } else {
                    coil.compose.AsyncImage(
                        model = formatImageUrl(imageUrl),
                        contentDescription = hotelPrice.hotel.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                // Bottom gradient scrim for legible text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xE6000000)),
                                startY = 120f
                            )
                        )
                )

                // Favorite Icon Button (top-left, like web)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .size(36.dp)
                        .background(Color(0x4D0C0C1D), CircleShape)
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Name + city overlaid at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        hotelPrice.hotel.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Place, null, tint = Color.White.copy(0.9f), modifier = Modifier.size(14.dp))
                        Text(hotelPrice.hotel.city, color = Color.White.copy(0.9f), fontSize = 13.sp)
                    }
                }
            }

            // Price + Book Now (matches web)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(formatPrice(hotelPrice.price), color = MoonPrimaryFixedDim, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("/ night", color = MoonOnSurfaceVariant.copy(0.6f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
                }
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MoonPrimaryFixedDim.copy(0.15f),
                        contentColor = MoonPrimaryFixedDim
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(1.dp, MoonPrimaryFixedDim),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Book Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun HotelImagePlaceholder(name: String) {
    val gradient = when (name) {
        "Zenith Sky Penthouse" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1F1235), Color(0xFF6B13AF), Color(0xFFFFB59C))
        )
        "Abyssal Sub-Villa" -> Brush.radialGradient(
            colors = listOf(Color(0xFF004E64), Color(0xFF001F2D))
        )
        "Neon District Loft" -> Brush.linearGradient(
            colors = listOf(Color(0xFF6B13AF), Color(0xFF121222))
        )
        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFF005228), Color(0xFF121222))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (name == "Zenith Sky Penthouse") {
                drawCircle(Color.White.copy(0.8f), radius = 2.dp.toPx(), center = Offset(width * 0.2f, height * 0.3f))
                drawCircle(Color.White.copy(0.5f), radius = 1.5.dp.toPx(), center = Offset(width * 0.8f, height * 0.2f))
            } else if (name == "Abyssal Sub-Villa") {
                drawCircle(MoonPrimaryFixedDim.copy(0.2f), radius = 20.dp.toPx(), center = Offset(width * 0.3f, height * 0.6f))
            } else if (name == "Neon District Loft") {
                drawLine(Color(0xFFFF007F).copy(0.2f), Offset(0f, height * 0.5f), Offset(width, height * 0.5f), 2.dp.toPx())
            } else {
                drawCircle(Color(0xFF60FF99).copy(0.1f), 30.dp.toPx(), Offset(width * 0.5f, height * 0.5f), style = Stroke(1.dp.toPx()))
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FavoritesTabContent(
    viewModel: DashboardViewModel,
    onNavigateToHotelDetail: (Int) -> Unit
) {
    val favHotels by viewModel.favoriteHotels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.fetchFavoriteHotels() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Favorite, null, tint = MoonPrimaryFixedDim, modifier = Modifier.size(26.dp))
                Text("Favorites", color = MoonPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text("Your favorite celestial stays", color = MoonOnSurfaceVariant, fontSize = 14.sp)
        }

        if (favHotels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No favorites yet. Browse hotels and add some!", color = MoonOnSurfaceVariant)
                }
            }
        } else {
            items(favHotels) { hotel ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                        .clickable { onNavigateToHotelDetail(hotel.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(hotel.name, color = MoonPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Place, null, tint = MoonOnSurfaceVariant, modifier = Modifier.size(14.dp))
                                Text(hotel.city, color = MoonOnSurfaceVariant, fontSize = 13.sp)
                            }
                        }
                        Button(
                            onClick = { viewModel.toggleFavorite(hotel.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF5A5A)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Remove", color = Color(0xFFFF7A7A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
    }
}

private val BOOKING_STATUS_OPTIONS = listOf(
    "ALL" to "All",
    "CONFIRMED" to "Confirmed",
    "PAYMENT_PENDING" to "Payment Pending",
    "RESERVED" to "Reserved",
    "GUEST_ADDED" to "Guest Added",
    "CANCELLED" to "Cancelled",
    "EXPIRED" to "Expired"
)

/** Status → (background, foreground) badge colors, mirroring the web status badges. */
private fun bookingStatusColors(status: String): Pair<Color, Color> = when (status.uppercase()) {
    "CONFIRMED", "PAID" -> Color(0x3300E479) to Color(0xFF00E479)
    "CANCELLED", "EXPIRED" -> Color(0x33FF5A5A) to Color(0xFFFF7A7A)
    else -> Color(0x33FFC24B) to Color(0xFFFFC24B)
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BookingsTabContent(viewModel: DashboardViewModel) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusFilter by viewModel.bookingStatusFilter.collectAsState()

    var cancelTarget by remember { mutableStateOf<BookingDto?>(null) }
    var detailBooking by remember { mutableStateOf<BookingDto?>(null) }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.fetchMyBookings() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Text("Your Escapes", color = MoonPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Verify and monitor your reservations history", color = MoonOnSurfaceVariant, fontSize = 14.sp)
        }

        // Status filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BOOKING_STATUS_OPTIONS) { (value, label) ->
                    val selected = statusFilter == value
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) MoonPrimaryFixedDim.copy(0.15f) else Color(0x0DFFFFFF))
                            .border(
                                1.dp,
                                if (selected) MoonPrimaryFixedDim else Color(0x1AFFFFFF),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.setBookingStatusFilter(value) }
                            .padding(vertical = 8.dp, horizontal = 14.dp)
                    ) {
                        Text(
                            label,
                            color = if (selected) MoonPrimaryFixedDim else MoonOnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        if (bookings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No bookings found.", color = MoonOnSurfaceVariant)
                }
            }
        } else {
            items(bookings) { booking ->
                val (badgeBg, badgeFg) = bookingStatusColors(booking.bookingStatus)
                val canCancel = booking.bookingStatus.uppercase() !in setOf("CANCELLED", "EXPIRED")
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                        .clickable { detailBooking = booking }
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(booking.hotelName, color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                if (booking.hotelCity.isNotBlank()) {
                                    Text(booking.hotelCity, color = MoonOnSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(badgeBg, RoundedCornerShape(8.dp))
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(booking.bookingStatus, color = badgeFg, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Room Type: ${booking.roomType}", color = MoonOnSurfaceVariant, fontSize = 13.sp)
                        Text("Duration: ${booking.checkInDate} to ${booking.checkOutDate}", color = MoonOnSurfaceVariant, fontSize = 13.sp)
                        Text("Rooms Booked: ${booking.roomsCount}", color = MoonOnSurfaceVariant, fontSize = 13.sp)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x1AFFFFFF))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (canCancel) {
                                Text(
                                    "Cancel",
                                    color = Color(0xFFFF7A7A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { cancelTarget = booking }
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                            Text(formatPrice(booking.totalAmount), color = MoonPrimaryFixedDim, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
    }

    // Booking detail dialog
    detailBooking?.let { b ->
        val (badgeBg, badgeFg) = bookingStatusColors(b.bookingStatus)
        AlertDialog(
            onDismissRequest = { detailBooking = null },
            confirmButton = {
                TextButton(onClick = { detailBooking = null }) {
                    Text("Close", color = MoonPrimaryFixedDim)
                }
            },
            title = { Text("Booking Details", color = MoonPrimary, fontWeight = FontWeight.Bold) },
            containerColor = Color(0xFF12121A),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BookingDetailRow("Hotel", b.hotelName)
                    if (b.hotelCity.isNotBlank()) BookingDetailRow("City", b.hotelCity)
                    BookingDetailRow("Room", b.roomType)
                    BookingDetailRow("Check-in", b.checkInDate)
                    BookingDetailRow("Check-out", b.checkOutDate)
                    BookingDetailRow("Rooms", b.roomsCount.toString())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Amount", color = MoonOnSurfaceVariant, fontSize = 13.sp)
                        Text(formatPrice(b.totalAmount), color = MoonPrimaryFixedDim, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", color = MoonOnSurfaceVariant, fontSize = 13.sp)
                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(6.dp))
                                .padding(vertical = 3.dp, horizontal = 8.dp)
                        ) {
                            Text(b.bookingStatus, color = badgeFg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        )
    }

    // Cancel confirmation dialog
    cancelTarget?.let { b ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            containerColor = Color(0xFF12121A),
            title = { Text("Cancel Booking", color = MoonPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to cancel this booking at ${b.hotelName}? A refund will be processed if applicable.",
                    color = MoonOnSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelBooking(b.id)
                    cancelTarget = null
                    detailBooking = null
                }) {
                    Text("Cancel Booking", color = Color(0xFFFF7A7A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) {
                    Text("Keep", color = MoonOnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun BookingDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MoonOnSurfaceVariant, fontSize = 13.sp)
        Text(value, color = MoonPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProfileTabContent(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // Avatar circle
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0x0DFFFFFF))
                .border(2.dp, MoonPrimaryFixedDim, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = MoonPrimaryFixedDim, modifier = Modifier.size(48.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(userName, color = MoonPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(userEmail, color = MoonOnSurfaceVariant, fontSize = 14.sp)
        }

        val isManager = viewModel.isHotelManager

        Box(
            modifier = Modifier
                .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Text(
                if (isManager) "ROLE: HOTEL MANAGER" else "ROLE: GUEST",
                color = MoonPrimaryFixedDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = {
                nameInput = userName
                showEditDialog = true
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x1AFFFFFF),
                contentColor = MoonPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Edit Profile Nickname")
        }

        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Nickname", color = MoonPrimary) },
                text = {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nickname") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MoonPrimary,
                            unfocusedTextColor = MoonPrimary,
                            focusedBorderColor = MoonPrimaryFixedDim,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                viewModel.updateProfileName(nameInput)
                                showEditDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Manager mode is only offered to accounts the backend marked as HOTEL_MANAGER
        if (isManager) {
            Button(
                onClick = { viewModel.setManagerMode(true) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MoonPrimaryFixedDim.copy(0.15f),
                    contentColor = MoonPrimaryFixedDim
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MoonPrimaryFixedDim)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.SwitchAccount, null, tint = MoonPrimaryFixedDim)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch to Manager Mode", color = MoonPrimaryFixedDim, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Log out button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF690005)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, null, tint = MoonPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = MoonPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DashboardBottomNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MoonSurface.copy(alpha = 0.9f))
            .border(width = 1.dp, color = Color(0x1AFFFFFF))
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val tabs = listOf(
            Triple("Explore", Icons.Default.Explore, "Explore"),
            Triple("Favorites", Icons.Default.FavoriteBorder, "Favorites"),
            Triple("Bookings", Icons.Default.ReceiptLong, "Bookings"),
            Triple("Profile", Icons.Default.Person, "Profile")
        )

        tabs.forEach { (tabName, icon, label) ->
            val isActive = activeTab == tabName
            val tintColor = if (isActive) MoonPrimaryFixedDim else MoonOnSurfaceVariant.copy(alpha = 0.6f)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable { onTabSelected(tabName) }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tintColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    color = tintColor,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// Utility border stroke generator
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)

fun formatImageUrl(url: String?): String? {
    if (url == null) return null
    if (url.startsWith("http://") || url.startsWith("https://")) {
        return url
    }
    val cleanUrl = if (url.startsWith("/")) url else "/$url"
    return "https://moonlight-stays-backend-d6hga6dtg6c3cya2.centralindia-01.azurewebsites.net/api/v1$cleanUrl"
}
