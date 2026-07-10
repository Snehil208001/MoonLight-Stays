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
import com.snehil.moon_stays_androidapp.core.util.formatPrice
import com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel.DashboardViewModel
import com.snehil.moon_stays_androidapp.mainui.dashboard.ui.HotelImagePlaceholder
import com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel.HotelDetailViewModel
import com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel.RoomDto
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurface
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurfaceVariant
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimary
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimaryFixedDim
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.style.TextAlign
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
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
    var showRoomDetailDialog by remember { mutableStateOf<RoomDto?>(null) }
    var showBookingDialog by remember { mutableStateOf(false) }
    var selectedBookingRoom by remember { mutableStateOf<RoomDto?>(null) }
    var bookingStep by remember { mutableStateOf(1) } // 1: Complete Booking, 2: Add Guest Details

    val guestsList = remember { mutableStateListOf<com.snehil.moon_stays_androidapp.data.remote.dto.GuestDto>() }
    var guestNameInput by remember { mutableStateOf("") }
    var guestGenderInput by remember { mutableStateOf("MALE") }
    var guestAgeInput by remember { mutableStateOf("") }

    var promoCodeInput by remember { mutableStateOf("") }
    val promoDiscountState by dashboardViewModel.promoDiscount.collectAsState()
    val promoErrorText by dashboardViewModel.promoError.collectAsState()
    val isPromoLoading by dashboardViewModel.isPromoLoading.collectAsState()
    val appliedPromoCode by dashboardViewModel.promoCode.collectAsState()

    var stripeCheckoutUrl by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // Clear state when dialog is closed
    LaunchedEffect(showBookingDialog) {
        if (!showBookingDialog) {
            bookingStep = 1
            guestsList.clear()
            guestNameInput = ""
            guestGenderInput = "MALE"
            guestAgeInput = ""
            promoCodeInput = ""
            dashboardViewModel.applyPromoCode("")
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
                    val isCurrentSelection = selectedBookingRoom?.id == room.id
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
                            .clickable { showRoomDetailDialog = room }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            val roomImageUrl = room.photos?.firstOrNull()
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                if (!roomImageUrl.isNullOrEmpty()) {
                                    coil.compose.AsyncImage(
                                        model = formatImageUrl(roomImageUrl),
                                        contentDescription = room.types,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    HotelImagePlaceholder(name = room.types)
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
                                                text = formatPrice(baselinePrice),
                                                color = MoonOnSurfaceVariant,
                                                fontSize = 11.sp,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                )
                                            )
                                        }
                                        Text(formatPrice(roomPrice), color = MoonPrimaryFixedDim, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Text("per night", color = MoonOnSurfaceVariant.copy(0.6f), fontSize = 9.sp)
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
                    val currentUserId by dashboardViewModel.userId.collectAsState()

                    val hasCompletedStay = bookings.any { b ->
                        b.hotelId == hotelId &&
                        (b.bookingStatus.equals("CONFIRMED", ignoreCase = true) || b.bookingStatus.equals("PAID", ignoreCase = true)) &&
                        try {
                            val checkout = java.time.LocalDate.parse(b.checkOutDate)
                            val today = java.time.LocalDate.now()
                            checkout.isBefore(today) || checkout.isEqual(today)
                        } catch (e: Exception) {
                            false
                        }
                    }

                    val alreadyReviewed = currentUserId != 0 && hotelReviews.any { r ->
                        r.userId == currentUserId
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
                                        Text(review.content, color = MoonOnSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    // 1. Room Detail Dialog
    showRoomDetailDialog?.let { room ->
        Dialog(
            onDismissRequest = { showRoomDetailDialog = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
                    .padding(0.dp)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(room.types, color = MoonPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(hotel?.name ?: "Unknown Hotel", color = MoonOnSurfaceVariant.copy(0.7f), fontSize = 12.sp)
                        }
                        IconButton(onClick = { showRoomDetailDialog = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MoonPrimary)
                        }
                    }

                    val roomPhotos = room.photos?.filter { !it.isNullOrEmpty() } ?: emptyList()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        if (roomPhotos.isEmpty()) {
                            HotelImagePlaceholder(name = room.types)
                        } else if (roomPhotos.size == 1) {
                            coil.compose.AsyncImage(
                                model = formatImageUrl(roomPhotos[0]),
                                contentDescription = room.types,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            val roomPagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { roomPhotos.size })
                            Box(modifier = Modifier.fillMaxSize()) {
                                androidx.compose.foundation.pager.HorizontalPager(
                                    state = roomPagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    coil.compose.AsyncImage(
                                        model = formatImageUrl(roomPhotos[page]),
                                        contentDescription = "${room.types} - Image ${page + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                // Page indicator dots
                                Row(
                                    Modifier
                                        .height(24.dp)
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Color(0x40000000)),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(roomPhotos.size) { iteration ->
                                        val color = if (roomPagerState.currentPage == iteration) MoonPrimaryFixedDim else Color.White.copy(alpha = 0.5f)
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

                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Group, null, tint = MoonPrimaryFixedDim, modifier = Modifier.size(18.dp))
                                Text("Capacity: ${room.capacity}", color = MoonPrimary, fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Home, null, tint = MoonPrimaryFixedDim, modifier = Modifier.size(18.dp))
                                Text("${room.totalCount} available", color = MoonPrimary, fontSize = 14.sp)
                            }
                        }

                        Text("Amenities:", color = MoonPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            room.amenities.forEach { amenity ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x14FFFFFF), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Text(amenity, color = MoonOnSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    val priceInfo = roomPrices.find { it.roomId == room.id }
                    val baselinePrice = priceInfo?.pricePerNight ?: room.basePrice
                    val roomPrice = promoDiscount?.let { discount ->
                        baselinePrice * (1.0 - discount / 100.0)
                    } ?: baselinePrice

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x0DFFFFFF))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${formatPrice(roomPrice)} / night", color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                selectedBookingRoom = room
                                bookingStep = 1
                                showBookingDialog = true
                                showRoomDetailDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MoonPrimaryFixedDim),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Book Now", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 2. Complete Your Booking Dialog
    if (showBookingDialog && bookingStep == 1) {
        Dialog(
            onDismissRequest = { showBookingDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
                    .padding(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Complete Your Booking", color = MoonPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showBookingDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MoonPrimary)
                        }
                    }

                    // Hotel Info
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hotel", color = MoonOnSurfaceVariant, fontSize = 14.sp)
                        Text(hotel?.name ?: "Celestial Stay", color = MoonPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Dates
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.DateRange, null, tint = MoonOnSurfaceVariant, modifier = Modifier.size(16.dp))
                            Text("Dates", color = MoonOnSurfaceVariant, fontSize = 14.sp)
                        }
                        Text("$checkInDate → $checkOutDate", color = MoonPrimary, fontSize = 14.sp)
                    }

                    // Rooms Count & Nights
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Rooms × Nights", color = MoonOnSurfaceVariant, fontSize = 14.sp)
                        Text("$roomsCount × $nights nights", color = MoonPrimary, fontSize = 14.sp)
                    }

                    Divider(color = Color(0x1AFFFFFF))

                    // Select Room list header
                    Text("Select Room", color = MoonPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    // Rooms selection options
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        rooms.forEach { room ->
                            val isSelected = selectedBookingRoom?.id == room.id
                            val roomPriceInfo = roomPrices.find { it.roomId == room.id }
                            val baselineRoomPrice = roomPriceInfo?.pricePerNight ?: room.basePrice
                            val displayPrice = promoDiscountState?.let { discount ->
                                baselineRoomPrice * (1.0 - discount / 100.0)
                            } ?: baselineRoomPrice

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MoonPrimaryFixedDim.copy(0.08f) else Color(0x05FFFFFF))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MoonPrimaryFixedDim else Color(0x1AFFFFFF),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedBookingRoom = room }
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val roomPhotoUrl = room.photos?.firstOrNull()
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    if (!roomPhotoUrl.isNullOrEmpty()) {
                                        coil.compose.AsyncImage(
                                            model = formatImageUrl(roomPhotoUrl),
                                            contentDescription = room.types,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        HotelImagePlaceholder(name = room.types)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(room.types, color = MoonPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Capacity: ${room.capacity}", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                                }
                                Text("${formatPrice(displayPrice)}/n", color = MoonPrimaryFixedDim, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Divider(color = Color(0x1AFFFFFF))

                    // Promo Code field
                    Text("Promo Code (optional)", color = MoonPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promoCodeInput,
                            onValueChange = { promoCodeInput = it },
                            placeholder = { Text("Enter code (e.g. LUNAR25)", color = MoonOnSurfaceVariant.copy(0.4f), fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MoonPrimaryFixedDim,
                                unfocusedBorderColor = Color(0x1AFFFFFF),
                                focusedTextColor = MoonPrimary,
                                unfocusedTextColor = MoonPrimary
                            )
                        )
                        Button(
                            onClick = { dashboardViewModel.applyPromoCode(promoCodeInput) },
                            enabled = promoCodeInput.isNotBlank() && !isPromoLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = MoonPrimaryFixedDim),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isPromoLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                            } else {
                                Text("Apply", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (promoDiscountState != null) {
                        Text("Applied: $promoDiscountState% discount successfully!", color = Color(0xFF00E479), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else if (promoErrorText != null) {
                        Text(promoErrorText!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Divider(color = Color(0x1AFFFFFF))

                    // Total Amount calculation
                    selectedBookingRoom?.let { room ->
                        val resPriceInfo = roomPrices.find { it.roomId == room.id }
                        val baselineTotal = resPriceInfo?.totalForStay ?: (room.basePrice * roomsCount * nights.toDouble())
                        val totalToPay = promoDiscountState?.let { discount ->
                            baselineTotal * (1.0 - discount / 100.0)
                        } ?: baselineTotal

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount:", color = MoonPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(formatPrice(totalToPay), color = MoonPrimaryFixedDim, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Bottom Action
                    Button(
                        onClick = {
                            if (selectedBookingRoom != null) {
                                bookingStep = 2
                            }
                        },
                        enabled = selectedBookingRoom != null,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MoonPrimaryFixedDim),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reserve & Add Guests", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // 3. Add Guest Details Dialog
    if (showBookingDialog && bookingStep == 2) {
        Dialog(
            onDismissRequest = { showBookingDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
                    .padding(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add Guest Details", color = MoonPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showBookingDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MoonPrimary)
                        }
                    }

                    Text("Add guest details for your booking. At least one guest is required to proceed.", color = MoonOnSurfaceVariant.copy(0.8f), fontSize = 12.sp)

                    // Guest Input Form Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x08FFFFFF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Add a guest", color = MoonPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            
                            OutlinedTextField(
                                value = guestNameInput,
                                onValueChange = { guestNameInput = it },
                                label = { Text("Full name", color = MoonOnSurfaceVariant) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MoonPrimaryFixedDim,
                                    unfocusedBorderColor = Color(0x1AFFFFFF),
                                    focusedTextColor = MoonPrimary,
                                    unfocusedTextColor = MoonPrimary
                                )
                            )

                            // Gender dropdown-style row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x08FFFFFF))
                                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(if (guestGenderInput == "MALE") "Male" else "Female", color = MoonPrimary, fontSize = 14.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "Male",
                                                color = if (guestGenderInput == "MALE") MoonPrimaryFixedDim else MoonOnSurfaceVariant,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable { guestGenderInput = "MALE" }
                                            )
                                            Text("|", color = MoonOnSurfaceVariant)
                                            Text(
                                                text = "Female",
                                                color = if (guestGenderInput == "FEMALE") MoonPrimaryFixedDim else MoonOnSurfaceVariant,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.clickable { guestGenderInput = "FEMALE" }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = guestAgeInput,
                                    onValueChange = { guestAgeInput = it },
                                    label = { Text("Age", color = MoonOnSurfaceVariant) },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MoonPrimaryFixedDim,
                                        unfocusedBorderColor = Color(0x1AFFFFFF),
                                        focusedTextColor = MoonPrimary,
                                        unfocusedTextColor = MoonPrimary
                                    )
                                )
                            }

                            Button(
                                onClick = {
                                    val name = guestNameInput.trim()
                                    val age = guestAgeInput.toIntOrNull()
                                    if (name.isNotEmpty() && age != null && age in 1..120) {
                                        guestsList.add(
                                            com.snehil.moon_stays_androidapp.data.remote.dto.GuestDto(
                                                name = name,
                                                gender = guestGenderInput,
                                                age = age
                                            )
                                        )
                                        guestNameInput = ""
                                        guestAgeInput = ""
                                    }
                                },
                                enabled = guestNameInput.isNotBlank() && guestAgeInput.toIntOrNull() != null,
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF))
                            ) {
                                Text("Add Guest", color = MoonPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Added Guests List
                    if (guestsList.isNotEmpty()) {
                        Text("Added Guests:", color = MoonPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            guestsList.forEachIndexed { index, g ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x05FFFFFF))
                                        .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(g.name, color = MoonPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("${g.gender.lowercase().replaceFirstChar { it.uppercase() }} · Age: ${g.age}", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { guestsList.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Guest", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color(0x1AFFFFFF))

                    // Total amount
                    selectedBookingRoom?.let { room ->
                        val resPriceInfo = roomPrices.find { it.roomId == room.id }
                        val baselineTotal = resPriceInfo?.totalForStay ?: (room.basePrice * roomsCount * nights.toDouble())
                        val totalToPay = promoDiscountState?.let { discount ->
                            baselineTotal * (1.0 - discount / 100.0)
                        } ?: baselineTotal

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total:", color = MoonPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(formatPrice(totalToPay), color = MoonPrimaryFixedDim, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Back & Proceed Bottom Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { bookingStep = 1 },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF))
                        ) {
                            Text("Back", color = MoonPrimary, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = {
                                selectedBookingRoom?.let { room ->
                                    val resPriceInfo = roomPrices.find { it.roomId == room.id }
                                    val baselineTotal = resPriceInfo?.totalForStay ?: (room.basePrice * roomsCount * nights.toDouble())
                                    val totalToPay = promoDiscountState?.let { discount ->
                                        baselineTotal * (1.0 - discount / 100.0)
                                    } ?: baselineTotal

                                    detailViewModel.bookRoom(
                                        hotelId = hotelId,
                                        roomId = room.id,
                                        checkInDate = checkInDate,
                                        checkOutDate = checkOutDate,
                                        roomsCount = roomsCount,
                                        totalAmount = totalToPay,
                                        guests = guestsList.toList(),
                                        promoCode = if (promoDiscountState != null) appliedPromoCode else null,
                                        onSuccess = { sessionUrl ->
                                            stripeCheckoutUrl = sessionUrl
                                            showBookingDialog = false
                                        }
                                    )
                                }
                            },
                            enabled = guestsList.isNotEmpty() && !isBookingLoading,
                            modifier = Modifier.weight(2f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MoonPrimaryFixedDim)
                        ) {
                            if (isBookingLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                            } else {
                                Text("Proceed to Payment", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. Stripe Checkout WebView Dialog
    if (!stripeCheckoutUrl.isNullOrEmpty()) {
        Dialog(
            onDismissRequest = { stripeCheckoutUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F16))
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    url: String?
                                ): Boolean {
                                    url?.let {
                                        if (it.contains("/payments/success")) {
                                            stripeCheckoutUrl = null
                                            showSuccessDialog = true

                                            // Pull the real, server-confirmed booking instead of
                                            // fabricating a local one.
                                            dashboardViewModel.fetchMyBookings()
                                            return true
                                        } else if (it.contains("/payments/failure") || it.contains("/payments/cancel")) {
                                            stripeCheckoutUrl = null
                                            showCancelDialog = true
                                            return true
                                        }
                                    }
                                    return false
                                }
                            }
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            loadUrl(stripeCheckoutUrl!!)
                        }
                    },
                    update = { view ->
                        stripeCheckoutUrl?.let {
                            if (view.url != it) {
                                view.loadUrl(it)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Header with Close Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Color(0xE60F0F16))
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Secure Stripe Checkout", color = MoonPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { stripeCheckoutUrl = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Checkout", tint = MoonPrimary)
                    }
                }
            }
        }
    }

    // 5. Success Dialog
    if (showSuccessDialog) {
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
                    .padding(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF00E479),
                        modifier = Modifier.size(64.dp)
                    )
                    Text("Booking Successful!", color = MoonPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(
                        text = "Your celestial stay has been reserved successfully! You can review details in the bookings history.",
                        color = MoonOnSurfaceVariant.copy(0.8f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MoonPrimaryFixedDim)
                    ) {
                        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 6. Cancel / Decline Dialog
    if (showCancelDialog) {
        Dialog(onDismissRequest = { showCancelDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
                    .padding(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Declined",
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Text("Payment Cancelled / Declined", color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(
                        text = "The payment session was cancelled or declined. Please check your payment details or try again.",
                        color = MoonOnSurfaceVariant.copy(0.8f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { showCancelDialog = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF))
                    ) {
                        Text("Dismiss", color = MoonPrimary, fontWeight = FontWeight.Bold)
                    }
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
