package com.snehil.moon_stays_androidapp.mainui.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel.DashboardViewModel
import com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel.Hotel
import com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel.RoomDto
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurface
import com.snehil.moon_stays_androidapp.ui.theme.MoonOnSurfaceVariant
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimary
import com.snehil.moon_stays_androidapp.ui.theme.MoonPrimaryFixedDim
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurface
import com.snehil.moon_stays_androidapp.ui.theme.MoonSurfaceContainerHighest

@Composable
fun ManagerDashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Hotels") }

    Scaffold(
        topBar = {
            DashboardTopBar(activeTab = "Manager: $activeTab")
        },
        bottomBar = {
            ManagerDashboardBottomNavBar(
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
                "Hotels" -> ManagerHotelsTab(viewModel)
                "Promos" -> ManagerPromosTab(viewModel)
                "Profile" -> ManagerProfileTab(viewModel, onLogout)
            }
        }
    }
}

@Composable
fun ManagerHotelsTab(viewModel: DashboardViewModel) {
    val hotels by viewModel.hotelsList.collectAsState()
    val roomsByHotel by viewModel.roomsByHotel.collectAsState()

    var showAddHotelDialog by remember { mutableStateOf(false) }
    var showAddRoomDialogForHotelId by remember { mutableStateOf<Int?>(null) }
    var showSurgeDialogForHotelId by remember { mutableStateOf<Int?>(null) }

    // Dialog state variables
    var hotelName by remember { mutableStateOf("") }
    var hotelCity by remember { mutableStateOf("") }
    var hotelAddress by remember { mutableStateOf("") }
    var hotelPhone by remember { mutableStateOf("") }
    var hotelEmail by remember { mutableStateOf("") }
    var hotelAmenities by remember { mutableStateOf("") }

    var roomType by remember { mutableStateOf("") }
    var roomPrice by remember { mutableStateOf("") }
    var roomCapacity by remember { mutableStateOf("") }
    var roomAmenities by remember { mutableStateOf("") }

    var surgeFactorInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hotel Inventory", color = MoonPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Manage structures, pricing and rooms", color = MoonOnSurfaceVariant, fontSize = 13.sp)
                }
                Button(
                    onClick = { showAddHotelDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MoonPrimaryFixedDim.copy(0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoonPrimaryFixedDim)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = MoonPrimaryFixedDim, modifier = Modifier.size(16.dp))
                        Text("Add Hotel", color = MoonPrimaryFixedDim, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        items(hotels) { hotel ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(hotel.name, color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(hotel.location, color = MoonOnSurfaceVariant, fontSize = 12.sp)
                            Text("Active Surge Factor: ${hotel.surgeFactor}x", color = MoonPrimaryFixedDim, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { showSurgeDialogForHotelId = hotel.id; surgeFactorInput = hotel.surgeFactor.toString() },
                                modifier = Modifier.size(36.dp).background(Color(0x14FFFFFF), CircleShape)
                            ) {
                                Icon(Icons.Default.TrendingUp, "Surge Factor", tint = MoonPrimary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { viewModel.deleteHotel(hotel.id) },
                                modifier = Modifier.size(36.dp).background(Color(0x1A690005), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val roomCount = roomsByHotel[hotel.id]?.size ?: 0
                        Text("$roomCount Room Categories", color = MoonPrimaryFixedDim, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MoonPrimary
                        )
                    }

                    if (expanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1AFFFFFF)))
                        Spacer(modifier = Modifier.height(8.dp))

                        val hotelRooms = roomsByHotel[hotel.id] ?: emptyList()
                        if (hotelRooms.isEmpty()) {
                            Text("No rooms configured. Add a category below.", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                        } else {
                            hotelRooms.forEach { room ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(room.types, color = MoonPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("₮${room.basePrice.toInt()} | Capacity: ${room.capacity}", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteRoom(hotel.id, room.id) },
                                        modifier = Modifier.size(32.dp).background(Color(0x0DFFFFFF), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete Room", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showAddRoomDialogForHotelId = hotel.id },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x14FFFFFF))
                        ) {
                            Text("Configure New Room Category", color = MoonPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 1. Add Hotel Dialog
    if (showAddHotelDialog) {
        AlertDialog(
            onDismissRequest = { showAddHotelDialog = false },
            title = { Text("Add Hotel", color = MoonPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = hotelName, onValueChange = { hotelName = it }, label = { Text("Hotel Name") })
                    OutlinedTextField(value = hotelCity, onValueChange = { hotelCity = it }, label = { Text("City") })
                    OutlinedTextField(value = hotelAddress, onValueChange = { hotelAddress = it }, label = { Text("Address") })
                    OutlinedTextField(value = hotelPhone, onValueChange = { hotelPhone = it }, label = { Text("Phone") })
                    OutlinedTextField(value = hotelEmail, onValueChange = { hotelEmail = it }, label = { Text("Email") })
                    OutlinedTextField(value = hotelAmenities, onValueChange = { hotelAmenities = it }, label = { Text("Amenities (comma-separated)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amList = hotelAmenities.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        viewModel.createHotel(hotelName, hotelCity, hotelAddress, hotelPhone, hotelEmail, amList)
                        showAddHotelDialog = false
                        // reset states
                        hotelName = ""; hotelCity = ""; hotelAddress = ""; hotelPhone = ""; hotelEmail = ""; hotelAmenities = ""
                    }
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showAddHotelDialog = false }) { Text("Cancel") } }
        )
    }

    // 2. Add Room Dialog
    showAddRoomDialogForHotelId?.let { hotelId ->
        AlertDialog(
            onDismissRequest = { showAddRoomDialogForHotelId = null },
            title = { Text("Add Room Category", color = MoonPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = roomType, onValueChange = { roomType = it }, label = { Text("Room Type (e.g. Deluxe Suite)") })
                    OutlinedTextField(value = roomPrice, onValueChange = { roomPrice = it }, label = { Text("Base Price Per Night") })
                    OutlinedTextField(value = roomCapacity, onValueChange = { roomCapacity = it }, label = { Text("Capacity (People)") })
                    OutlinedTextField(value = roomAmenities, onValueChange = { roomAmenities = it }, label = { Text("Amenities (comma-separated)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = roomPrice.toDoubleOrNull() ?: 300.0
                        val capVal = roomCapacity.toIntOrNull() ?: 2
                        val amList = roomAmenities.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        viewModel.createRoom(hotelId, roomType, priceVal, capVal, amList)
                        showAddRoomDialogForHotelId = null
                        roomType = ""; roomPrice = ""; roomCapacity = ""; roomAmenities = ""
                    }
                ) { Text("Create Room") }
            },
            dismissButton = { TextButton(onClick = { showAddRoomDialogForHotelId = null }) { Text("Cancel") } }
        )
    }

    // 3. Set Surge Factor Dialog
    showSurgeDialogForHotelId?.let { hotelId ->
        AlertDialog(
            onDismissRequest = { showSurgeDialogForHotelId = null },
            title = { Text("Configure Dynamic Surge Factor", color = MoonPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Apply a surge factor multiplier (e.g., 1.25 for holiday surges) to all base prices.", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                    OutlinedTextField(value = surgeFactorInput, onValueChange = { surgeFactorInput = it }, label = { Text("Surge Factor") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val factor = surgeFactorInput.toDoubleOrNull() ?: 1.0
                        viewModel.setSurgeFactor(hotelId, factor)
                        showSurgeDialogForHotelId = null
                    }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showSurgeDialogForHotelId = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ManagerPromosTab(viewModel: DashboardViewModel) {
    val promos by viewModel.promoCodes.collectAsState()

    var showAddPromoDialog by remember { mutableStateOf(false) }
    var promoCodeInput by remember { mutableStateOf("") }
    var promoDiscountInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Promo Codes", color = MoonPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Active coupons and guest discounts", color = MoonOnSurfaceVariant, fontSize = 13.sp)
                }
                Button(
                    onClick = { showAddPromoDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MoonPrimaryFixedDim.copy(0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoonPrimaryFixedDim)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = MoonPrimaryFixedDim, modifier = Modifier.size(16.dp))
                        Text("Add Promo", color = MoonPrimaryFixedDim, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        items(promos) { promo ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(promo.code, color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Discount: ${promo.discountPercentage}% off", color = MoonOnSurfaceVariant, fontSize = 13.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Switch(
                            checked = promo.active,
                            onCheckedChange = { viewModel.togglePromoCodeActive(promo.id) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MoonPrimaryFixedDim,
                                checkedTrackColor = MoonPrimaryFixedDim.copy(0.3f)
                            )
                        )
                        IconButton(
                            onClick = { viewModel.deletePromoCode(promo.id) },
                            modifier = Modifier.size(36.dp).background(Color(0x1A690005), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, "Delete Promo", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddPromoDialog) {
        AlertDialog(
            onDismissRequest = { showAddPromoDialog = false },
            title = { Text("Add Promo Code", color = MoonPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = promoCodeInput, onValueChange = { promoCodeInput = it }, label = { Text("Promo Code (e.g. LUNAR50)") })
                    OutlinedTextField(value = promoDiscountInput, onValueChange = { promoDiscountInput = it }, label = { Text("Discount Percentage") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val disc = promoDiscountInput.toIntOrNull() ?: 15
                        viewModel.createPromoCode(promoCodeInput, disc)
                        showAddPromoDialog = false
                        promoCodeInput = ""; promoDiscountInput = ""
                    }
                ) { Text("Create Promo") }
            },
            dismissButton = { TextButton(onClick = { showAddPromoDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ManagerProfileTab(
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
            Text("Voyager Manager", color = MoonPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("manager@celestial.com", color = MoonOnSurfaceVariant, fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Text("ROLE: HOTEL_MANAGER", color = MoonPrimaryFixedDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Switch to Guest Mode Button
        Button(
            onClick = { viewModel.setManagerMode(false) },
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
                Text("Switch to Guest Mode", color = MoonPrimaryFixedDim, fontWeight = FontWeight.Bold)
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
fun ManagerDashboardBottomNavBar(
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
            Triple("Hotels", Icons.Default.Apartment, "Hotels"),
            Triple("Promos", Icons.Default.Tag, "Promos"),
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
