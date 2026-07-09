package com.snehil.moon_stays_androidapp.mainui.hoteldetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel.BookingDto
import com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel.DashboardViewModel
import com.snehil.moon_stays_androidapp.mainui.dashboard.ui.HotelImagePlaceholder
import com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel.HotelDetailViewModel
import com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel.RoomDto
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurface
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurfaceVariant
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimary
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimaryFixedDim
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurface
import kotlin.math.max

@Composable
fun HotelDetailScreen(
    hotelId: Int,
    dashboardViewModel: DashboardViewModel,
    detailViewModel: HotelDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hotel = remember(hotelId) { dashboardViewModel.getHotelById(hotelId) }
    val rooms by detailViewModel.rooms.collectAsState()
    val roomPrices by detailViewModel.roomPrices.collectAsState()
    val averageRating by detailViewModel.averageRating.collectAsState()

    val checkInDate by dashboardViewModel.checkInDate.collectAsState()
    val checkOutDate by dashboardViewModel.checkOutDate.collectAsState()
    val roomsCount by dashboardViewModel.roomsCount.collectAsState()
    val promoDiscount by dashboardViewModel.promoDiscount.collectAsState()

    val isBookingLoading by detailViewModel.isBookingLoading.collectAsState()
    val isBookingSuccess by detailViewModel.isBookingSuccess.collectAsState()

    val nights = remember(checkInDate, checkOutDate) {
        try {
            val checkIn = java.time.LocalDate.parse(checkInDate)
            val checkOut = java.time.LocalDate.parse(checkOutDate)
            java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut).coerceAtLeast(1)
        } catch (e: Exception) {
            2L
        }
    }

    LaunchedEffect(hotelId, checkInDate, checkOutDate, roomsCount) {
        detailViewModel.fetchHotelDetails(hotelId)
        detailViewModel.fetchRoomPrices(hotelId, checkInDate, checkOutDate, roomsCount)
    }

    var selectedRoom by remember { mutableStateOf<RoomDto?>(null) }

    // Navigation trigger on success
    LaunchedEffect(isBookingSuccess) {
        if (isBookingSuccess) {
            selectedRoom?.let { room ->
                val priceInfo = roomPrices.find { it.roomId == room.id }
                val baselineTotal = priceInfo?.totalForStay ?: (room.basePrice * roomsCount * nights.toDouble())
                val discountedTotal = promoDiscount?.let { discount ->
                    baselineTotal * (1.0 - discount / 100.0)
                } ?: baselineTotal

                dashboardViewModel.addBooking(
                    BookingDto(
                        id = (100..999).random(),
                        hotelId = hotelId,
                        hotelName = hotel?.name ?: "Celestial Stay",
                        roomType = room.types,
                        checkInDate = checkInDate,
                        checkOutDate = checkOutDate,
                        totalAmount = discountedTotal,
                        roomsCount = roomsCount
                    )
                )
            }
            detailViewModel.resetSuccessState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            HotelDetailTopBar(
                hotelName = hotel?.name ?: "Celestial Hotel",
                onBackClick = onNavigateBack
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(MoonSurface),
        containerColor = MoonSurface
    ) { innerPadding ->
        if (hotel == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Hotel not found", color = MoonPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Header Visual Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        val photos = hotel.photos.filter { !it.isNullOrEmpty() }
                        if (photos.isEmpty()) {
                            HotelImagePlaceholder(name = hotel.name)
                        } else if (photos.size == 1) {
                            coil.compose.AsyncImage(
                                model = formatImageUrl(photos[0]),
                                contentDescription = hotel.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { photos.size })
                            Box(modifier = Modifier.fillMaxSize()) {
                                androidx.compose.foundation.pager.HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    coil.compose.AsyncImage(
                                        model = formatImageUrl(photos[page]),
                                        contentDescription = "${hotel.name} - Image ${page + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                // Pager Indicator
                                Row(
                                    Modifier
                                        .height(24.dp)
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Color(0x40000000)),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(photos.size) { iteration ->
                                        val color = if (pagerState.currentPage == iteration) MoonPrimaryFixedDim else Color.White.copy(alpha = 0.5f)
                                        Box(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .size(6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Hotel Details Title & Amenities
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(hotel.name, color = MoonPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text(hotel.location, color = MoonOnSurfaceVariant, fontSize = 13.sp)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (averageRating > 0.0) String.format("%.1f", averageRating) else "New",
                                    color = MoonPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Amenities offered at this location:",
                            color = MoonPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            hotel.amenities.forEach { amenity ->
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

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x1AFFFFFF))
                    )
                }

                // Room Options List
                item {
                    Text("Select Suite Category", color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                items(rooms) { room ->
                    val isCurrentSelection = selectedRoom?.id == room.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentSelection) MoonPrimaryFixedDim.copy(0.08f) else Color(0x0DFFFFFF)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isCurrentSelection) MoonPrimaryFixedDim else Color(0x1AFFFFFF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedRoom = room }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            val roomImageUrl = room.photos?.firstOrNull()
                            if (!roomImageUrl.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    coil.compose.AsyncImage(
                                        model = formatImageUrl(roomImageUrl),
                                        contentDescription = room.types,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(room.types, color = MoonPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Group, null, tint = MoonOnSurfaceVariant, modifier = Modifier.size(16.dp))
                                        Text("Capacity: ${room.capacity}", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                                    }
                                }

                                val priceInfo = roomPrices.find { it.roomId == room.id }
                                val baselinePrice = priceInfo?.pricePerNight ?: room.basePrice
                                val roomPrice = promoDiscount?.let { discount ->
                                    baselinePrice * (1.0 - discount / 100.0)
                                } ?: baselinePrice

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        room.amenities.forEach { amenity ->
                                            Text("• $amenity", color = MoonOnSurfaceVariant, fontSize = 11.sp)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        if (promoDiscount != null) {
                                            Text(
                                                text = "₮${baselinePrice.toInt()}",
                                                color = MoonOnSurfaceVariant,
                                                fontSize = 11.sp,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                )
                                            )
                                        }
                                        Text("₮${roomPrice.toInt()}", color = MoonPrimaryFixedDim, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Text("per night", color = MoonOnSurfaceVariant.copy(0.6f), fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Booking Checkout Button Card
                selectedRoom?.let { room ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Reservation details:", color = MoonPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Dates: $checkInDate to $checkOutDate ($nights nights)", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                                Text("Rooms Count: $roomsCount", color = MoonOnSurfaceVariant, fontSize = 12.sp)

                                val resPriceInfo = roomPrices.find { it.roomId == room.id }
                                val baselineTotal = resPriceInfo?.totalForStay ?: (room.basePrice * roomsCount * nights.toDouble())
                                val totalToPay = promoDiscount?.let { discount ->
                                    baselineTotal * (1.0 - discount / 100.0)
                                } ?: baselineTotal

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Amount Due:", color = MoonPrimary, fontSize = 14.sp)
                                    Text("₮${totalToPay.toInt()}", color = MoonPrimaryFixedDim, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                }

                                Button(
                                    onClick = {
                                        detailViewModel.bookRoom(
                                            hotelId = hotelId,
                                            roomId = room.id,
                                            checkInDate = checkInDate,
                                            checkOutDate = checkOutDate,
                                            roomsCount = roomsCount,
                                            totalAmount = totalToPay,
                                            onSuccess = { /* handled in LaunchedEffect */ }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MoonPrimaryFixedDim.copy(0.15f),
                                        contentColor = MoonPrimaryFixedDim
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isBookingLoading
                                ) {
                                    if (isBookingLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MoonPrimaryFixedDim, strokeWidth = 2.dp)
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("Book via Stripe Checkout", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Icon(Icons.Default.KeyboardArrowRight, null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Reviews Section
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))
                }

                item {
                    val reviewsMap by detailViewModel.reviews.collectAsState()
                    val hotelReviews = reviewsMap[hotelId] ?: emptyList()
                    val bookings by dashboardViewModel.bookings.collectAsState()
                    val currentUserName by dashboardViewModel.userName.collectAsState()

                    val hasCompletedStay = bookings.any { b ->
                        b.hotelId == hotelId && try {
                            val checkout = java.time.LocalDate.parse(b.checkOutDate)
                            val today = java.time.LocalDate.now()
                            checkout.isBefore(today) || checkout.isEqual(today)
                        } catch (e: Exception) {
                            false
                        }
                    }

                    val alreadyReviewed = hotelReviews.any { r ->
                        r.userName.equals(currentUserName, ignoreCase = true)
                    }

                    val canReview = hasCompletedStay && !alreadyReviewed

                    var showReviewForm by remember { mutableStateOf(false) }
                    var ratingSelected by remember { mutableStateOf(5) }
                    var reviewContentInput by remember { mutableStateOf("") }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reviews (${hotelReviews.size})", color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            if (canReview && !showReviewForm) {
                                Text(
                                    text = "Add Review",
                                    color = MoonPrimaryFixedDim,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showReviewForm = true }
                                )
                            }
                        }

                        if (showReviewForm) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Add Your Review", color = MoonPrimary, fontWeight = FontWeight.Bold)
                                    
                                    // Star Selector
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        (1..5).forEach { starIndex ->
                                            val isFilled = starIndex <= ratingSelected
                                            Icon(
                                                imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = if (isFilled) Color(0xFFFFB59C) else MoonOnSurfaceVariant,
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clickable { ratingSelected = starIndex }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = reviewContentInput,
                                        onValueChange = { reviewContentInput = it },
                                        placeholder = { Text("What did you think of your escape?", color = MoonOnSurfaceVariant.copy(0.4f), fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MoonPrimaryFixedDim,
                                            unfocusedBorderColor = Color(0x1AFFFFFF),
                                            focusedTextColor = MoonPrimary,
                                            unfocusedTextColor = MoonPrimary
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { showReviewForm = false }) {
                                            Text("Cancel", color = MoonOnSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                if (reviewContentInput.isNotBlank()) {
                                                    detailViewModel.addReview(hotelId, ratingSelected, reviewContentInput)
                                                    showReviewForm = false
                                                    reviewContentInput = ""
                                                    ratingSelected = 5
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MoonPrimaryFixedDim.copy(0.2f))
                                        ) {
                                            Text("Submit", color = MoonPrimaryFixedDim)
                                        }
                                    }
                                }
                            }
                        }

                        // Reviews list
                        if (hotelReviews.isEmpty()) {
                            Text("No reviews yet. Share your experience!", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                        } else {
                            hotelReviews.forEach { review ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0x05FFFFFF)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(review.userName, color = MoonPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                (1..5).forEach { star ->
                                                    Icon(
                                                        imageVector = if (star <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                                        contentDescription = null,
                                                        tint = if (star <= review.rating) Color(0xFFFFB59C) else MoonOnSurfaceVariant,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(review.content, color = MoonOnSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun HotelDetailTopBar(
    hotelName: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MoonSurface.copy(alpha = 0.9f))
            .border(width = 1.dp, color = Color(0x1AFFFFFF))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0x08FFFFFF), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back Icon",
                tint = MoonPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = hotelName,
            color = MoonPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatImageUrl(url: String?): String? {
    if (url == null) return null
    if (url.startsWith("http://") || url.startsWith("https://")) {
        return url
    }
    val cleanUrl = if (url.startsWith("/")) url else "/$url"
    return "https://moonlight-stays-backend-d6hga6dtg6c3cya2.centralindia-01.azurewebsites.net/api/v1$cleanUrl"
}
