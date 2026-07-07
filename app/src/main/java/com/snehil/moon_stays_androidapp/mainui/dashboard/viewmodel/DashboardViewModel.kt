package com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel

import androidx.lifecycle.viewModelScope
import com.snehil.moon_stays_androidapp.core.base.BaseViewModel
import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelDto
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelSearchRequest
import com.snehil.moon_stays_androidapp.domain.usecase.GetMyBookingsUseCase
import com.snehil.moon_stays_androidapp.domain.usecase.SearchHotelsUseCase
import com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel.RoomDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
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

// Helper mapping extensions
fun HotelDto.toDomain(): Hotel {
    return Hotel(
        id = this.id.toInt(),
        name = this.name,
        city = this.city,
        photos = this.photos ?: emptyList(),
        amenities = this.amenities ?: emptyList(),
        basePrice = 100.0,
        location = this.contactInfo?.location ?: "",
        address = this.contactInfo?.address ?: "",
        phoneNumber = this.contactInfo?.phoneNumber ?: "",
        email = this.contactInfo?.email ?: "",
        surgeFactor = 1.0
    )
}

fun com.snehil.moon_stays_androidapp.data.remote.dto.HotelPriceDto.toDomain(): HotelPriceDto {
    return HotelPriceDto(
        hotel = this.hotel.toDomain(),
        price = this.price
    )
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val searchHotelsUseCase: SearchHotelsUseCase,
    private val getMyBookingsUseCase: GetMyBookingsUseCase
) : BaseViewModel() {

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
    private val _bookings = MutableStateFlow<List<BookingDto>>(emptyList())
    val bookings: StateFlow<List<BookingDto>> = _bookings.asStateFlow()

    // 6. Mutable Hotel & Room Sources for management
    private val _hotelsList = MutableStateFlow<List<Hotel>>(emptyList())
    val hotelsList: StateFlow<List<Hotel>> = _hotelsList.asStateFlow()

    // Rooms managed by hotel ID
    private val _roomsByHotel = MutableStateFlow<Map<Int, List<RoomDto>>>(emptyMap())
    val roomsByHotel: StateFlow<Map<Int, List<RoomDto>>> = _roomsByHotel.asStateFlow()

    init {
        triggerSearch()
        fetchMyBookings()
    }

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
        launchSafe {
            val req = HotelSearchRequest(
                city = _city.value.ifBlank { null },
                checkInDate = _checkInDate.value,
                endDate = _checkOutDate.value,
                roomsCount = _roomsCount.value,
                page = 0,
                size = 50
            )
            searchHotelsUseCase(req).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                        _errorMessage.value = null
                    }
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        val domainList = result.data.map { it.toDomain() }
                        _hotelsList.value = domainList.map { it.hotel }
                    }
                }
            }
        }
    }

    fun fetchMyBookings() {
        launchSafe {
            getMyBookingsUseCase(0, 50, null).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {}
                    is NetworkResult.Error -> {
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        val mapped = result.data.content.map { dto ->
                            BookingDto(
                                id = dto.id.toInt(),
                                hotelId = dto.hotel.id.toInt(),
                                hotelName = dto.hotel.name,
                                roomType = dto.room.types,
                                checkInDate = dto.checkInDate,
                                checkOutDate = dto.checkOutDate,
                                totalAmount = dto.amount.toDouble(),
                                roomsCount = dto.roomsCount
                            )
                        }
                        _bookings.value = mapped
                    }
                }
            }
        }
    }

    fun setManagerMode(enabled: Boolean) {
        _isManagerMode.value = enabled
    }

    // 7. Promo Validation & Management Actions
    fun applyPromoCode(code: String) {
        _promoCode.value = code.uppercase()
        val match = _promoCodes.value.find { it.code.equals(code, ignoreCase = true) && it.active }
        if (match != null) {
            _promoDiscount.value = match.discountPercentage
        } else {
            _promoDiscount.value = null
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
            basePrice = 500.0,
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
