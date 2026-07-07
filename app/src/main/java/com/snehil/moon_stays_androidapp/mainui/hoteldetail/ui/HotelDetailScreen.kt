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

    LaunchedEffect(hotelId) {
        detailViewModel.fetchHotelDetails(hotelId)
    }

    val checkInDate by dashboardViewModel.checkInDate.collectAsState()
    val checkOutDate by dashboardViewModel.checkOutDate.collectAsState()
    val roomsCount by dashboardViewModel.roomsCount.collectAsState()
    val promoDiscount by dashboardViewModel.promoDiscount.collectAsState()

    val isBookingLoading by detailViewModel.isBookingLoading.collectAsState()
    val isBookingSuccess by detailViewModel.isBookingSuccess.collectAsState()

    var selectedRoom by remember { mutableStateOf<RoomDto?>(null) }

    // Navigation trigger on success
    LaunchedEffect(isBookingSuccess) {
        if (isBookingSuccess) {
            selectedRoom?.let { room ->
                // Calculate discounted price
                val baseTotal = room.basePrice * roomsCount * 2.0 // Assuming a 2-night stay for mock calculations
                val discountedTotal = promoDiscount?.let { discount ->
                    baseTotal * (1.0 - discount / 100.0)
                } ?: baseTotal

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
                        HotelImagePlaceholder(name = hotel.name)
                    }
                }

                // Hotel Details Title & Amenities
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(hotel.name, color = MoonPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(hotel.location, color = MoonOnSurfaceVariant, fontSize = 13.sp)

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
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                            // Dynamic room price reflecting active promo discount
                            val roomPrice = promoDiscount?.let { discount ->
                                room.basePrice * (1.0 - discount / 100.0)
                            } ?: room.basePrice

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
                                            text = "₮${room.basePrice.toInt()}",
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
                                Text("Dates: $checkInDate to $checkOutDate (2 nights)", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                                Text("Rooms Count: $roomsCount", color = MoonOnSurfaceVariant, fontSize = 12.sp)

                                val calculatedRoomPrice = promoDiscount?.let { discount ->
                                    room.basePrice * (1.0 - discount / 100.0)
                                } ?: room.basePrice
                                val totalToPay = calculatedRoomPrice * roomsCount * 2.0

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
                            if (!showReviewForm) {
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
