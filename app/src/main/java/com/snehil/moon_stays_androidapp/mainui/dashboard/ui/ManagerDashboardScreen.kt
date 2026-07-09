package com.snehil.moon_stays_androidapp.mainui.dashboard.ui

import android.app.DatePickerDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import java.util.Calendar

data class DeleteConfirmState(
    val type: String, // "hotel", "room", "promo"
    val hotelId: Int? = null,
    val roomId: Int? = null,
    val promoId: Int? = null
)

@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedMonth = String.format("%02d", selectedMonth + 1)
            val formattedDay = String.format("%02d", selectedDay)
            onValueChange("$selectedYear-$formattedMonth-$formattedDay")
        },
        year,
        month,
        day
    )
    
    Box(
        modifier = modifier.clickable { datePickerDialog.show() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color(0x1AFFFFFF),
                disabledTextColor = Color.White,
                disabledLabelColor = MoonOnSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .clickable { datePickerDialog.show() }
        )
    }
}

fun getFileFromUri(context: android.content.Context, uri: android.net.Uri): java.io.File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = java.io.File.createTempFile("upload_", ".jpg", context.cacheDir)
        tempFile.deleteOnExit()
        val outputStream = java.io.FileOutputStream(tempFile)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun PhotosInputView(
    photos: List<String>,
    onPhotosChange: (List<String>) -> Unit,
    onUploadClick: (java.io.File, (String?) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var photoUrlInput by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    val selectImageLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val file = getFileFromUri(context, it)
            if (file != null) {
                isUploading = true
                onUploadClick(file) { uploadedUrl ->
                    isUploading = false
                    if (uploadedUrl != null) {
                        onPhotosChange(photos + uploadedUrl)
                    }
                }
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Photos", color = MoonOnSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        
        photos.forEach { url ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Link, null, tint = MoonPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (url.length > 30) "..." + url.takeLast(27) else url,
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onPhotosChange(photos.filter { it != url }) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, "Remove URL", tint = Color.Red, modifier = Modifier.size(14.dp))
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = photoUrlInput,
                onValueChange = { photoUrlInput = it },
                placeholder = { Text("Paste photo URL", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MoonPrimaryFixedDim,
                    unfocusedBorderColor = Color(0x1AFFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Button(
                onClick = {
                    if (photoUrlInput.isNotBlank()) {
                        onPhotosChange(photos + photoUrlInput.trim())
                        photoUrlInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MoonPrimaryFixedDim.copy(0.15f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add", color = MoonPrimaryFixedDim, fontSize = 12.sp)
            }
        }

        Button(
            onClick = { selectImageLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MoonPrimaryFixedDim,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !isUploading
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Photo from Device", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ManagerDashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Hotels") }
    var showDeleteConfirm by remember { mutableStateOf<DeleteConfirmState?>(null) }

    // Intercept back press: go back to Hotels tab if on another tab, otherwise exit Manager Mode
    androidx.activity.compose.BackHandler(enabled = true) {
        if (activeTab != "Hotels") {
            activeTab = "Hotels"
        } else {
            viewModel.setManagerMode(false)
        }
    }

    val context = LocalContext.current
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

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
                "Hotels" -> ManagerHotelsTab(viewModel, onDeleteRequest = { showDeleteConfirm = it })
                "Promos" -> ManagerPromosTab(viewModel, onDeleteRequest = { showDeleteConfirm = it })
                "Profile" -> ManagerProfileTab(viewModel, onLogout)
            }
        }
    }

    // 4. Deletion Confirmation Dialog
    showDeleteConfirm?.let { state ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = {
                Text(
                    text = when (state.type) {
                        "hotel" -> "Delete Hotel?"
                        "room" -> "Delete Room Category?"
                        else -> "Delete Promo Code?"
                    },
                    color = MoonPrimary
                )
            },
            text = {
                Text(
                    text = when (state.type) {
                        "hotel" -> "This will permanently delete the hotel and all its rooms. This cannot be undone."
                        "room" -> "This will permanently delete the room. This cannot be undone."
                        else -> "This will permanently delete the promo code. This cannot be undone."
                    },
                    color = MoonOnSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (state.type) {
                            "hotel" -> state.hotelId?.let { viewModel.deleteHotel(it) }
                            "room" -> {
                                if (state.hotelId != null && state.roomId != null) {
                                    viewModel.deleteRoom(state.hotelId, state.roomId)
                                }
                            }
                            "promo" -> state.promoId?.let { viewModel.deletePromoCode(it) }
                        }
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF690005))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ManagerHotelsTab(
    viewModel: DashboardViewModel,
    onDeleteRequest: (DeleteConfirmState) -> Unit
) {
    val hotels by viewModel.hotelsList.collectAsState()
    val roomsByHotel by viewModel.roomsByHotel.collectAsState()

    var showAddHotelDialog by remember { mutableStateOf(false) }
    var showAddRoomDialogForHotelId by remember { mutableStateOf<Int?>(null) }
    var showSurgeDialogForHotelId by remember { mutableStateOf<Int?>(null) }
    var editingHotel by remember { mutableStateOf<Hotel?>(null) }
    var editingRoom by remember { mutableStateOf<RoomDto?>(null) }
    var editingRoomForHotelId by remember { mutableStateOf<Int?>(null) }

    // Dialog state variables
    var hotelName by remember { mutableStateOf("") }
    var hotelCity by remember { mutableStateOf("") }
    var hotelAddress by remember { mutableStateOf("") }
    var hotelPhone by remember { mutableStateOf("") }
    var hotelEmail by remember { mutableStateOf("") }
    var hotelAmenities by remember { mutableStateOf("") }
    var hotelPhotos by remember { mutableStateOf<List<String>>(emptyList()) }

    var roomType by remember { mutableStateOf("") }
    var roomPrice by remember { mutableStateOf("") }
    var roomCapacity by remember { mutableStateOf("") }
    var roomTotalCount by remember { mutableStateOf("1") }
    var roomAmenities by remember { mutableStateOf("") }
    var roomPhotos by remember { mutableStateOf<List<String>>(emptyList()) }

    var surgeFactorInput by remember { mutableStateOf("") }
    var surgeStartDate by remember { mutableStateOf("") }
    var surgeEndDate by remember { mutableStateOf("") }

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
                    onClick = { 
                        editingHotel = null
                        hotelName = ""
                        hotelCity = ""
                        hotelAddress = ""
                        hotelPhone = ""
                        hotelEmail = ""
                        hotelAmenities = ""
                        hotelPhotos = emptyList()
                        showAddHotelDialog = true 
                    },
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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(hotel.name, color = MoonPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (hotel.active) Color(0x3300FF00) else Color(0x33FF9800),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (hotel.active) "Active" else "Inactive",
                                    color = if (hotel.active) Color.Green else Color(0xFFFF9800),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(hotel.location, color = MoonOnSurfaceVariant, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Active status toggle chip
                            Row(
                                modifier = Modifier
                                    .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.toggleHotelStatus(hotel.id) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (hotel.active) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                                    contentDescription = "Toggle Status",
                                    tint = if (hotel.active) Color.Green else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (hotel.active) "Disable" else "Enable",
                                    color = MoonOnSurface,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Edit button
                            Row(
                                modifier = Modifier
                                    .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                                    .clickable {
                                        editingHotel = hotel
                                        hotelName = hotel.name
                                        hotelCity = hotel.city
                                        hotelAddress = hotel.address
                                        hotelPhone = hotel.phoneNumber
                                        hotelEmail = hotel.email
                                        hotelAmenities = hotel.amenities.joinToString(", ")
                                        hotelPhotos = hotel.photos
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Edit, "Edit", tint = MoonPrimary, modifier = Modifier.size(14.dp))
                                Text("Edit", color = MoonOnSurface, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }

                            // Surge pricing button
                            Row(
                                modifier = Modifier
                                    .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                                    .clickable {
                                        showSurgeDialogForHotelId = hotel.id
                                        surgeFactorInput = hotel.surgeFactor.toString()
                                        surgeStartDate = ""
                                        surgeEndDate = ""
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.TrendingUp, "Surge Factor", tint = MoonPrimary, modifier = Modifier.size(14.dp))
                                Text("Surge", color = MoonOnSurface, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Delete button
                            Row(
                                modifier = Modifier
                                    .background(Color(0x1A690005), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0x4DFF0000), RoundedCornerShape(8.dp))
                                    .clickable { onDeleteRequest(DeleteConfirmState("hotel", hotelId = hotel.id)) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                                Text("Delete", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Medium)
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(room.types, color = MoonPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("₮${room.basePrice.toInt()} | Cap: ${room.capacity} | Count: ${room.totalCount ?: 1}", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingRoom = room
                                                editingRoomForHotelId = hotel.id
                                                roomType = room.types
                                                roomPrice = room.basePrice.toInt().toString()
                                                roomTotalCount = (room.totalCount ?: 1).toString()
                                                roomCapacity = (room.capacity ?: 2).toString()
                                                roomAmenities = room.amenities?.joinToString(", ") ?: ""
                                                roomPhotos = room.photos ?: emptyList()
                                            },
                                            modifier = Modifier.size(32.dp).background(Color(0x0DFFFFFF), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Edit, "Edit Room", tint = MoonPrimary, modifier = Modifier.size(14.dp))
                                        }
                                        IconButton(
                                            onClick = { onDeleteRequest(DeleteConfirmState("room", hotelId = hotel.id, roomId = room.id.toInt())) },
                                            modifier = Modifier.size(32.dp).background(Color(0x1A690005), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Delete, "Delete Room", tint = Color.Red, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                editingRoom = null
                                editingRoomForHotelId = hotel.id
                                roomType = ""
                                roomPrice = ""
                                roomTotalCount = "1"
                                roomCapacity = ""
                                roomAmenities = ""
                                roomPhotos = emptyList()
                                showAddRoomDialogForHotelId = hotel.id 
                            },
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

    // 1. Add / Edit Hotel Dialog
    if (showAddHotelDialog || editingHotel != null) {
        val isEdit = editingHotel != null
        AlertDialog(
            onDismissRequest = { showAddHotelDialog = false; editingHotel = null },
            title = { Text(if (isEdit) "Edit Hotel" else "Add Hotel", color = MoonPrimary) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    item {
                        OutlinedTextField(value = hotelName, onValueChange = { hotelName = it }, label = { Text("Hotel Name") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = hotelCity, onValueChange = { hotelCity = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = hotelAddress, onValueChange = { hotelAddress = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = hotelPhone, onValueChange = { hotelPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = hotelEmail, onValueChange = { hotelEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = hotelAmenities, onValueChange = { hotelAmenities = it }, label = { Text("Amenities (comma-separated)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        PhotosInputView(
                            photos = hotelPhotos,
                            onPhotosChange = { hotelPhotos = it },
                            onUploadClick = { file, callback ->
                                viewModel.uploadImage(file, callback)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amList = hotelAmenities.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (isEdit) {
                            editingHotel?.let {
                                viewModel.updateHotel(it.id, hotelName, hotelCity, hotelAddress, hotelPhone, hotelEmail, amList, hotelPhotos)
                            }
                        } else {
                            viewModel.createHotel(hotelName, hotelCity, hotelAddress, hotelPhone, hotelEmail, amList, hotelPhotos)
                        }
                        showAddHotelDialog = false
                        editingHotel = null
                        hotelName = ""; hotelCity = ""; hotelAddress = ""; hotelPhone = ""; hotelEmail = ""; hotelAmenities = ""; hotelPhotos = emptyList()
                    }
                ) { Text(if (isEdit) "Save" else "Create") }
            },
            dismissButton = { 
                TextButton(
                    onClick = { 
                        showAddHotelDialog = false
                        editingHotel = null
                        hotelName = ""; hotelCity = ""; hotelAddress = ""; hotelPhone = ""; hotelEmail = ""; hotelAmenities = ""; hotelPhotos = emptyList()
                    }
                ) { Text("Cancel") } 
            }
        )
    }

    // 2. Add / Edit Room Dialog
    if (showAddRoomDialogForHotelId != null || editingRoom != null) {
        val isEdit = editingRoom != null
        val hotelId = showAddRoomDialogForHotelId ?: editingRoomForHotelId ?: 0
        AlertDialog(
            onDismissRequest = { showAddRoomDialogForHotelId = null; editingRoom = null },
            title = { Text(if (isEdit) "Edit Room Category" else "Add Room Category", color = MoonPrimary) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    item {
                        OutlinedTextField(value = roomType, onValueChange = { roomType = it }, label = { Text("Room Type (e.g. Deluxe Suite)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = roomPrice, onValueChange = { roomPrice = it }, label = { Text("Base Price Per Night (₹)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = roomTotalCount, onValueChange = { roomTotalCount = it }, label = { Text("Total Count") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = roomCapacity, onValueChange = { roomCapacity = it }, label = { Text("Capacity (People)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = roomAmenities, onValueChange = { roomAmenities = it }, label = { Text("Amenities (comma-separated)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        PhotosInputView(
                            photos = roomPhotos,
                            onPhotosChange = { roomPhotos = it },
                            onUploadClick = { file, callback ->
                                viewModel.uploadImage(file, callback)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = roomPrice.toDoubleOrNull() ?: 300.0
                        val countVal = roomTotalCount.toIntOrNull() ?: 1
                        val capVal = roomCapacity.toIntOrNull() ?: 2
                        val amList = roomAmenities.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (isEdit) {
                            editingRoom?.let {
                                viewModel.updateRoom(hotelId, it.id.toInt(), roomType, priceVal, capVal, countVal, amList, roomPhotos)
                            }
                        } else {
                            viewModel.createRoom(hotelId, roomType, priceVal, capVal, countVal, amList, roomPhotos)
                        }
                        showAddRoomDialogForHotelId = null
                        editingRoom = null
                        roomType = ""; roomPrice = ""; roomTotalCount = "1"; roomCapacity = ""; roomAmenities = ""; roomPhotos = emptyList()
                    }
                ) { Text(if (isEdit) "Save" else "Create") }
            },
            dismissButton = { 
                TextButton(
                    onClick = { 
                        showAddRoomDialogForHotelId = null
                        editingRoom = null
                        roomType = ""; roomPrice = ""; roomTotalCount = "1"; roomCapacity = ""; roomAmenities = ""; roomPhotos = emptyList()
                    }
                ) { Text("Cancel") } 
            }
        )
    }

    // 3. Set Surge Factor Dialog
    showSurgeDialogForHotelId?.let { hotelId ->
        AlertDialog(
            onDismissRequest = { showSurgeDialogForHotelId = null },
            title = { Text("Configure Dynamic Surge Factor", color = MoonPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Apply a surge factor multiplier (e.g., 1.25 for holiday surges) to all base prices.", color = MoonOnSurfaceVariant, fontSize = 12.sp)
                    OutlinedTextField(
                        value = surgeFactorInput,
                        onValueChange = { surgeFactorInput = it },
                        label = { Text("Surge Factor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DatePickerField(
                        label = "Start Date",
                        value = surgeStartDate,
                        onValueChange = { surgeStartDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DatePickerField(
                        label = "End Date",
                        value = surgeEndDate,
                        onValueChange = { surgeEndDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val factor = surgeFactorInput.toDoubleOrNull() ?: 1.0
                        if (surgeStartDate.isNotEmpty() && surgeEndDate.isNotEmpty()) {
                            viewModel.setSurgeFactor(hotelId, factor, surgeStartDate, surgeEndDate)
                            showSurgeDialogForHotelId = null
                        }
                    },
                    enabled = surgeStartDate.isNotEmpty() && surgeEndDate.isNotEmpty()
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showSurgeDialogForHotelId = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ManagerPromosTab(
    viewModel: DashboardViewModel,
    onDeleteRequest: (DeleteConfirmState) -> Unit
) {
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
                            onClick = { onDeleteRequest(DeleteConfirmState("promo", promoId = promo.id)) },
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

        Box(
            modifier = Modifier
                .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            Text("ROLE: HOTEL_MANAGER", color = MoonPrimaryFixedDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
