package com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel.RoomDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Hotel(
    val id: Int,
    val name: String,
    val city: String,
    val photos: List<String>,
    val amenities: List<String>,
    val basePrice: Double,
    val location: String,
    val address: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val surgeFactor: Double = 1.0
)

data class HotelPriceDto(
    val hotel: Hotel,
    val price: Double
)

data class BookingDto(
    val id: Int,
    val hotelId: Int,
    val hotelName: String,
    val roomType: String,
    val checkInDate: String,
    val checkOutDate: String,
    val totalAmount: Double,
    val roomsCount: Int
)

data class PromoCodeDto(
    val id: Int,
    val code: String,
    val discountPercentage: Int,
    val active: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    // 1. Session Manager State
    private val _isManagerMode = MutableStateFlow(false)
    val isManagerMode: StateFlow<Boolean> = _isManagerMode.asStateFlow()

    // 2. Search fields states
    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _checkInDate = MutableStateFlow("2026-07-06")
    val checkInDate: StateFlow<String> = _checkInDate.asStateFlow()

    private val _checkOutDate = MutableStateFlow("2026-07-07")
    val checkOutDate: StateFlow<String> = _checkOutDate.asStateFlow()

    private val _roomsCount = MutableStateFlow(1)
    val roomsCount: StateFlow<Int> = _roomsCount.asStateFlow()

    private val _selectedRoomType = MutableStateFlow("All")
    val selectedRoomType: StateFlow<String> = _selectedRoomType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 3. Promo Code States
    private val _promoCode = MutableStateFlow("")
    val promoCode: StateFlow<String> = _promoCode.asStateFlow()

    private val _promoDiscount = MutableStateFlow<Int?>(null)
    val promoDiscount: StateFlow<Int?> = _promoDiscount.asStateFlow()

    private val _promoCodes = MutableStateFlow(
        listOf(
            PromoCodeDto(1, "LUNAR25", 25, true),
            PromoCodeDto(2, "STELLAR10", 10, true)
        )
    )
    val promoCodes: StateFlow<List<PromoCodeDto>> = _promoCodes.asStateFlow()

    // 4. Favorites States
    private val _favoriteIds = MutableStateFlow<Set<Int>>(setOf())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    // 5. Bookings History States
    private val _bookings = MutableStateFlow<List<BookingDto>>(
        listOf(
            BookingDto(
                id = 101,
                hotelId = 1,
                hotelName = "Zenith Sky Penthouse",
                roomType = "Premium Suite",
                checkInDate = "2026-06-15",
                checkOutDate = "2026-06-18",
                totalAmount = 2550.0,
                roomsCount = 1
            )
        )
    )
    val bookings: StateFlow<List<BookingDto>> = _bookings.asStateFlow()

    // 6. Mutable Hotel & Room Sources for management
    private val _hotelsList = MutableStateFlow(
        listOf(
            Hotel(
                id = 1,
                name = "Zenith Sky Penthouse",
                city = "Neo-Tokyo",
                photos = listOf("zenith"),
                amenities = listOf("Quantum Wifi", "Oxygen Bar", "Gravity Control"),
                basePrice = 850.0,
                location = "Neo-Tokyo, Sector 7",
                address = "Starlight Way, Sector 7",
                phoneNumber = "+81-90-1234-5678",
                email = "zenith@stays.moon"
            ),
            Hotel(
                id = 2,
                name = "Abyssal Sub-Villa",
                city = "Mariana Basin",
                photos = listOf("abyssal"),
                amenities = listOf("Panoramic Glass", "Deepsea Submarine", "Private Chef"),
                basePrice = 1200.0,
                location = "Mariana Basin One",
                address = "Underwater Rift 1",
                phoneNumber = "+1-800-DEEPSEA",
                email = "abyssal@stays.moon"
            ),
            Hotel(
                id = 3,
                name = "Neon District Loft",
                city = "Neo-Tokyo",
                photos = listOf("neon"),
                amenities = listOf("Holo Screens", "Soundproof Walls", "Cyber bar"),
                basePrice = 540.0,
                location = "Sector 8 Night Market",
                address = "Neon Boulevard 45",
                phoneNumber = "+81-80-8888-9999",
                email = "neon@stays.moon"
            ),
            Hotel(
                id = 4,
                name = "Eden Bio-Dome Resort",
                city = "Nova Forest",
                photos = listOf("eden"),
                amenities = listOf("Lush Gardens", "Fresh O2 Loop", "Natural Lake"),
                basePrice = 620.0,
                location = "Nova Forest Preserve",
                address = "Eco Dome 2",
                phoneNumber = "+44-20-7946-0192",
                email = "eden@stays.moon"
            )
        )
    )
    val hotelsList: StateFlow<List<Hotel>> = _hotelsList.asStateFlow()

    // Rooms managed by hotel ID
    private val _roomsByHotel = MutableStateFlow<Map<Int, List<RoomDto>>>(
        mapOf(
            1 to listOf(
                RoomDto(11, "Standard Capsule", 850.0, 1, listOf("Quantum Wifi", "Clean Air")),
                RoomDto(12, "Zenith Sky Suite", 1450.0, 2, listOf("Gravity Control", "Personal Spa", "Transparent Ceiling"))
            ),
            2 to listOf(
                RoomDto(21, "Neptune Sub Room", 1200.0, 2, listOf("Underwater View", "Private Bath")),
                RoomDto(22, "Ocean Abyssal Suite", 2200.0, 4, listOf("Panoramic Dome View", "Private Submarine Ride"))
            ),
            3 to listOf(
                RoomDto(31, "Neon Cyber Bed", 540.0, 1, listOf("Holo Screens", "Soundproof")),
                RoomDto(32, "Retro Neon Loft", 980.0, 2, listOf("Infinity Bath", "Private Bar Access", "Synth Sound System"))
            ),
            4 to listOf(
                RoomDto(41, "Green Bio Capsule", 620.0, 2, listOf("Natural Light", "Lush Forest Air")),
                RoomDto(42, "Starlight Lake Villa", 1150.0, 3, listOf("Bio-dome lake access", "Organic breakfast", "Yoga terrace"))
            )
        )
    )
    val roomsByHotel: StateFlow<Map<Int, List<RoomDto>>> = _roomsByHotel.asStateFlow()

    // Combined search results (filtering _hotelsList dynamically)
    val hotels: StateFlow<List<HotelPriceDto>> = combine(_hotelsList, _city, _selectedRoomType) { list, cityQuery, roomType ->
        list.filter { hotel ->
            val matchesCity = cityQuery.isBlank() || hotel.city.contains(cityQuery, ignoreCase = true)
            val matchesRoomType = roomType == "All" || hotel.amenities.any { it.contains(roomType, ignoreCase = true) }
            matchesCity && matchesRoomType
        }.map { hotel ->
            val discountPrice = _promoDiscount.value?.let { discount ->
                hotel.basePrice * (1.0 - discount / 100.0)
            } ?: hotel.basePrice
            // Apply surge pricing factor if any
            val finalPrice = discountPrice * hotel.surgeFactor
            HotelPriceDto(hotel, finalPrice)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onCityChanged(newCity: String) {
        _city.value = newCity
    }

    fun onDatesChanged(checkIn: String, checkOut: String) {
        _checkInDate.value = checkIn
        _checkOutDate.value = checkOut
    }

    fun onRoomsCountChanged(count: Int) {
        _roomsCount.value = count
    }

    fun onRoomTypeSelected(roomType: String) {
        _selectedRoomType.value = roomType
    }

    fun triggerSearch() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1000)
            _isLoading.value = false
        }
    }

    fun setManagerMode(enabled: Boolean) {
        _isManagerMode.value = enabled
    }

    // 7. Promo Validation & Management Actions
    fun applyPromoCode(code: String) {
        viewModelScope.launch {
            _promoCode.value = code.uppercase()
            val match = _promoCodes.value.find { it.code.equals(code, ignoreCase = true) && it.active }
            if (match != null) {
                _promoDiscount.value = match.discountPercentage
            } else {
                _promoDiscount.value = null
            }
        }
    }

    fun createPromoCode(code: String, discount: Int) {
        val newPromo = PromoCodeDto(
            id = (_promoCodes.value.maxOfOrNull { it.id } ?: 0) + 1,
            code = code.uppercase(),
            discountPercentage = discount,
            active = true
        )
        _promoCodes.value = _promoCodes.value + newPromo
    }

    fun deletePromoCode(promoId: Int) {
        _promoCodes.value = _promoCodes.value.filter { it.id != promoId }
        _promoDiscount.value = null
    }

    fun togglePromoCodeActive(promoId: Int) {
        _promoCodes.value = _promoCodes.value.map {
            if (it.id == promoId) it.copy(active = !it.active) else it
        }
    }

    // 8. Hotel Management CRUD
    fun createHotel(
        name: String,
        city: String,
        address: String,
        phone: String,
        email: String,
        amenities: List<String>
    ) {
        val nextId = (_hotelsList.value.maxOfOrNull { it.id } ?: 0) + 1
        val newHotel = Hotel(
            id = nextId,
            name = name,
            city = city,
            photos = listOf("default"),
            amenities = amenities,
            basePrice = 500.0, // default placeholder base price
            location = "$city, $address",
            address = address,
            phoneNumber = phone,
            email = email
        )
        _hotelsList.value = _hotelsList.value + newHotel
        _roomsByHotel.value = _roomsByHotel.value + (nextId to emptyList())
    }

    fun updateHotel(
        hotelId: Int,
        name: String,
        city: String,
        address: String,
        phone: String,
        email: String,
        amenities: List<String>
    ) {
        _hotelsList.value = _hotelsList.value.map {
            if (it.id == hotelId) {
                it.copy(
                    name = name,
                    city = city,
                    location = "$city, $address",
                    address = address,
                    phoneNumber = phone,
                    email = email,
                    amenities = amenities
                )
            } else it
        }
    }

    fun deleteHotel(hotelId: Int) {
        _hotelsList.value = _hotelsList.value.filter { it.id != hotelId }
        _roomsByHotel.value = _roomsByHotel.value - hotelId
    }

    fun setSurgeFactor(hotelId: Int, surgeFactor: Double) {
        _hotelsList.value = _hotelsList.value.map {
            if (it.id == hotelId) it.copy(surgeFactor = surgeFactor) else it
        }
    }

    // 9. Room Management CRUD
    fun createRoom(
        hotelId: Int,
        types: String,
        basePrice: Double,
        capacity: Int,
        amenities: List<String>
    ) {
        val currentRooms = _roomsByHotel.value[hotelId] ?: emptyList()
        val nextRoomId = (currentRooms.maxOfOrNull { it.id } ?: (hotelId * 10)) + 1
        val newRoom = RoomDto(
            id = nextRoomId,
            types = types,
            basePrice = basePrice,
            capacity = capacity,
            amenities = amenities
        )
        _roomsByHotel.value = _roomsByHotel.value + (hotelId to (currentRooms + newRoom))
    }

    fun deleteRoom(hotelId: Int, roomId: Int) {
        val currentRooms = _roomsByHotel.value[hotelId] ?: emptyList()
        val updatedRooms = currentRooms.filter { it.id != roomId }
        _roomsByHotel.value = _roomsByHotel.value + (hotelId to updatedRooms)
    }

    fun toggleFavorite(hotelId: Int) {
        val currentFavs = _favoriteIds.value
        if (currentFavs.contains(hotelId)) {
            _favoriteIds.value = currentFavs - hotelId
        } else {
            _favoriteIds.value = currentFavs + hotelId
        }
    }

    fun addBooking(booking: BookingDto) {
        _bookings.value = _bookings.value + booking
    }

    fun getHotelById(hotelId: Int): Hotel? {
        return _hotelsList.value.find { it.id == hotelId }
    }
}
