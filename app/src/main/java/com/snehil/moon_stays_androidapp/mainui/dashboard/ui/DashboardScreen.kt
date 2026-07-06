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
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Explore") }

    Scaffold(
        topBar = {
            DashboardTopBar(activeTab = activeTab)
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
fun DashboardTopBar(activeTab: String) {
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

@Composable
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
    val hotels by viewModel.hotels.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var promoInput by remember { mutableStateOf("") }

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

                    // Dates Grid
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = checkInDate,
                            onValueChange = { /* Ignore direct change for simplicity */ },
                            label = { Text("Check-In", color = MoonOnSurfaceVariant) },
                            leadingIcon = { Icon(Icons.Default.DateRange, null, tint = MoonOnSurfaceVariant) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = Color(0x1AFFFFFF),
                                disabledTextColor = MoonPrimary,
                                disabledLabelColor = MoonOnSurfaceVariant
                            )
                        )
                        OutlinedTextField(
                            value = checkOutDate,
                            onValueChange = { /* Ignore */ },
                            label = { Text("Check-Out", color = MoonOnSurfaceVariant) },
                            leadingIcon = { Icon(Icons.Default.DateRange, null, tint = MoonOnSurfaceVariant) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = Color(0x1AFFFFFF),
                                disabledTextColor = MoonPrimary,
                                disabledLabelColor = MoonOnSurfaceVariant
                            )
                        )
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

                    // Search Button
                    Button(
                        onClick = viewModel::triggerSearch,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x14FFFFFF)),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text("Apply", color = MoonPrimary)
                    }
                }
                if (promoDiscount != null) {
                    Text(
                        text = "Promo applied: $promoDiscount% off base prices!",
                        color = Color(0xFF00E479),
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
            // Visual Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                HotelImagePlaceholder(name = hotelPrice.hotel.name)

                // Favorite Icon Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .background(Color(0x4D0C0C1D), CircleShape)
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else MoonPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Info Details
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(hotelPrice.hotel.name, color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(hotelPrice.hotel.location, color = MoonOnSurfaceVariant, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₮${hotelPrice.price.toInt()}", color = MoonPrimaryFixedDim, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("per night", color = MoonOnSurfaceVariant.copy(0.6f), fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Amenities Chips Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    hotelPrice.hotel.amenities.take(2).forEach { amenity ->
                        Box(
                            modifier = Modifier
                                .background(Color(0x14FFFFFF), RoundedCornerShape(8.dp))
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Text(amenity, color = MoonOnSurface, fontSize = 10.sp)
                        }
                    }
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

@Composable
fun FavoritesTabContent(
    viewModel: DashboardViewModel,
    onNavigateToHotelDetail: (Int) -> Unit
) {
    val hotels by viewModel.hotels.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    val favHotels = hotels.filter { favoriteIds.contains(it.hotel.id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Wishlist", color = MoonPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Your favorite celestial stays", color = MoonOnSurfaceVariant, fontSize = 14.sp)
        }

        if (favHotels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No favorited hotels yet.", color = MoonOnSurfaceVariant)
                }
            }
        } else {
            items(favHotels) { hotelPrice ->
                HotelCard(
                    hotelPrice = hotelPrice,
                    isFavorite = true,
                    onToggleFavorite = { viewModel.toggleFavorite(hotelPrice.hotel.id) },
                    onClick = { onNavigateToHotelDetail(hotelPrice.hotel.id) }
                )
            }
        }
    }
}

@Composable
fun BookingsTabContent(viewModel: DashboardViewModel) {
    val bookings by viewModel.bookings.collectAsState()

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

        if (bookings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active bookings found.", color = MoonOnSurfaceVariant)
                }
            }
        } else {
            items(bookings) { booking ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(booking.hotelName, color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(Color(0x3300E479), RoundedCornerShape(8.dp))
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text("CONFIRMED", color = Color(0xFF00E479), fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Paid via Stripe", color = MoonOnSurfaceVariant.copy(0.6f), fontSize = 11.sp)
                            Text("Total Amount: ₮${booking.totalAmount.toInt()}", color = MoonPrimaryFixedDim, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTabContent(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit
) {
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
            Text("Voyager Snehi", color = MoonPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("snehi@celestial.com", color = MoonOnSurfaceVariant, fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Text("ROLE: GUEST", color = MoonPrimaryFixedDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Switch to Manager Mode Button
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
