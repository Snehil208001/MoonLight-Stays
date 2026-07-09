package com.snehil.moon_stays_androidapp.mainui.dashboard.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.snehil.moon_stays_androidapp.core.base.BaseViewModel
import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelDto
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelSearchRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelContactInfo
import com.snehil.moon_stays_androidapp.data.remote.dto.SurgeUpdateDto
import com.snehil.moon_stays_androidapp.data.remote.dto.PromoCodeDto as RemotePromoCodeDto
import com.snehil.moon_stays_androidapp.domain.repository.AuthRepository
import com.snehil.moon_stays_androidapp.domain.repository.AdminRepository
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

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
    val surgeFactor: Double = 1.0,
    val active: Boolean = false
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
        id = this.id?.toInt() ?: 0,
        name = this.name ?: "Unknown Hotel",
        city = this.city ?: "Unknown City",
        photos = this.photos ?: emptyList(),
        amenities = this.amenities ?: emptyList(),
        basePrice = 100.0,
        location = this.contactInfo?.location ?: "",
        address = this.contactInfo?.address ?: "",
        phoneNumber = this.contactInfo?.phoneNumber ?: "",
        email = this.contactInfo?.email ?: "",
        surgeFactor = 1.0,
        active = this.active ?: false
    )
}

fun com.snehil.moon_stays_androidapp.data.remote.dto.HotelPriceDto.toDomain(): HotelPriceDto {
    return HotelPriceDto(
        hotel = this.hotel.toDomain(),
        price = this.price
    )
}

fun RemotePromoCodeDto.toDomain(): PromoCodeDto {
    return PromoCodeDto(
        id = this.id?.toInt() ?: 0,
        code = this.code,
        discountPercentage = this.discountPercentage,
        active = this.active ?: true
    )
}

fun com.snehil.moon_stays_androidapp.data.remote.dto.RoomDto.toDomain(): RoomDto {
    return RoomDto(
        id = this.id?.toInt() ?: 0,
        types = this.types ?: "Unknown Type",
        basePrice = this.basePrice?.toDouble() ?: 0.0,
        capacity = this.capacity ?: 2,
        amenities = this.amenities ?: emptyList(),
        totalCount = this.totalCount ?: 1,
        photos = this.photos ?: emptyList()
    )
}

fun RoomDto.toRemote(): com.snehil.moon_stays_androidapp.data.remote.dto.RoomDto {
    return com.snehil.moon_stays_androidapp.data.remote.dto.RoomDto(
        id = if (this.id == 0) null else this.id.toLong(),
        types = this.types,
        basePrice = BigDecimal.valueOf(this.basePrice),
        photos = this.photos,
        amenities = this.amenities,
        totalCount = this.totalCount,
        capacity = this.capacity
    )
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val searchHotelsUseCase: SearchHotelsUseCase,
    private val getMyBookingsUseCase: GetMyBookingsUseCase,
    private val authRepository: AuthRepository,
    private val adminRepository: AdminRepository
) : BaseViewModel() {

    // 1. Session Manager State
    private val _isManagerMode = MutableStateFlow(false)
    val isManagerMode: StateFlow<Boolean> = _isManagerMode.asStateFlow()

    // 2. Search fields states
    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _checkInDate = MutableStateFlow(java.time.LocalDate.now().toString())
    val checkInDate: StateFlow<String> = _checkInDate.asStateFlow()

    private val _checkOutDate = MutableStateFlow(java.time.LocalDate.now().plusDays(1).toString())
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

    private val _searchResults = MutableStateFlow<List<HotelPriceDto>>(emptyList())
    val searchResults: StateFlow<List<HotelPriceDto>> = _searchResults.asStateFlow()

    init {
        triggerSearch()
        fetchMyBookings()
        fetchFavoriteHotels()
        fetchUserProfile()
    }

    // Combined search results (filtering _searchResults dynamically by amenity filter tabs)
    val hotels: StateFlow<List<HotelPriceDto>> = combine(_searchResults, _selectedRoomType) { list, roomType ->
        list.filter { hotelPrice ->
            val hotel = hotelPrice.hotel
            val matchesRoomType = roomType == "All" || hotel.amenities.any { it.contains(roomType, ignoreCase = true) }
            matchesRoomType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _userName = MutableStateFlow(authRepository.getUserName() ?: "Voyager")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(authRepository.getUserEmail() ?: "voyager@celestial.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    fun fetchUserProfile() {
        launchSafe {
            val user = authRepository.fetchProfile()
            user?.let {
                it.name?.let { name ->
                    authRepository.saveUserName(name)
                    _userName.value = name
                }
                it.email?.let { email ->
                    authRepository.saveUserEmail(email)
                    _userEmail.value = email
                }
            }
        }
    }

    fun updateProfileName(newName: String) {
        Log.d("DashboardViewModel", "updateProfileName - newName: $newName")
        launchSafe {
            authRepository.updateProfile(newName).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val updatedName = result.data.name ?: newName
                        _userName.value = updatedName
                        Log.d("DashboardViewModel", "updateProfileName - Success! Updated profile name to: $updatedName")
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "updateProfileName - Error: ${result.message}")
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

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
                        Log.d("DashboardViewModel", "triggerSearch - Success! Loaded ${domainList.size} search results")
                        _searchResults.value = domainList
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
                                 hotelId = dto.hotel.id?.toInt() ?: 0,
                                 hotelName = dto.hotel.name ?: "Unknown Hotel",
                                 roomType = dto.room.types ?: "Unknown Room Type",
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

    // Role comes from the backend profile saved at login (see LoginUseCase);
    // HOTEL_MANAGER is assigned server-side, never granted from the UI.
    val isHotelManager: Boolean
        get() = authRepository.isManager()

    fun setManagerMode(enabled: Boolean) {
        val managerEnabled = enabled && authRepository.isManager()
        Log.d("DashboardViewModel", "setManagerMode - enabled: $enabled, isManagerUser: ${authRepository.isManager()}, result: $managerEnabled")
        _isManagerMode.value = managerEnabled
        if (managerEnabled) {
            loadManagerData()
        }
    }

    fun logout() {
        authRepository.clearSession()
        _isManagerMode.value = false
    }

    // Load all manager specific data from the backend
    fun loadManagerData() {
        fetchManagerHotels()
        fetchManagerPromos()
    }

    fun fetchManagerHotels() {
        Log.d("DashboardViewModel", "fetchManagerHotels - Start fetching manager hotels")
        launchSafe {
            adminRepository.getMyHotels().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        Log.d("DashboardViewModel", "fetchManagerHotels - Loading...")
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "fetchManagerHotels - Error: ${result.message}")
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "fetchManagerHotels - Success! Received ${result.data.size} hotels from backend")
                        _isLoading.value = false
                        val domainHotels = result.data.map { it.toDomain() }
                        _hotelsList.value = domainHotels
                        domainHotels.forEach { hotel ->
                            Log.d("DashboardViewModel", "fetchManagerHotels - Hotel: id=${hotel.id}, name=${hotel.name}, active=${hotel.active}")
                            fetchRoomsForHotel(hotel.id.toLong())
                        }
                    }
                }
            }
        }
    }

    fun fetchRoomsForHotel(hotelId: Long) {
        Log.d("DashboardViewModel", "fetchRoomsForHotel - Fetching rooms for hotelId: $hotelId")
        launchSafe {
            adminRepository.getHotelRooms(hotelId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "fetchRoomsForHotel - Success! Received ${result.data.size} rooms for hotelId: $hotelId")
                        val currentRooms = _roomsByHotel.value.toMutableMap()
                        currentRooms[hotelId.toInt()] = result.data.map { it.toDomain() }
                        _roomsByHotel.value = currentRooms
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "fetchRoomsForHotel - Error fetching rooms for hotelId $hotelId: ${result.message}")
                    }
                    is NetworkResult.Loading -> {
                        Log.d("DashboardViewModel", "fetchRoomsForHotel - Loading rooms for hotelId $hotelId...")
                    }
                }
            }
        }
    }

    fun fetchManagerPromos() {
        launchSafe {
            adminRepository.getPromoCodes().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _isLoading.value = true
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _promoCodes.value = result.data.map { it.toDomain() }
                    }
                }
            }
        }
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
        launchSafe {
            val remotePromo = RemotePromoCodeDto(
                id = null,
                code = code.uppercase(),
                discountPercentage = discount,
                active = true
            )
            adminRepository.createPromoCode(remotePromo).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _isLoading.value = true
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        fetchManagerPromos()
                    }
                }
            }
        }
    }

    fun deletePromoCode(promoId: Int) {
        launchSafe {
            adminRepository.deletePromoCode(promoId.toLong()).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _isLoading.value = true
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        fetchManagerPromos()
                        _promoDiscount.value = null
                    }
                }
            }
        }
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
        amenities: List<String>,
        photos: List<String> = emptyList()
    ) {
        Log.d("DashboardViewModel", "createHotel - Request: name=$name, city=$city, amenities=$amenities")
        launchSafe {
            val contactInfo = HotelContactInfo(
                address = address,
                phoneNumber = phone,
                email = email,
                location = "$city, $address"
            )
            val hotelDto = HotelDto(
                id = null,
                name = name,
                city = city,
                photos = photos,
                amenities = amenities,
                contactInfo = contactInfo,
                active = false
            )
            adminRepository.createHotel(hotelDto).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        Log.d("DashboardViewModel", "createHotel - Loading...")
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "createHotel - Failed: ${result.message}")
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "createHotel - Success! New Hotel ID: ${result.data.id}")
                        _isLoading.value = false
                        fetchManagerHotels()
                    }
                }
            }
        }
    }

    fun updateHotel(
        hotelId: Int,
        name: String,
        city: String,
        address: String,
        phone: String,
        email: String,
        amenities: List<String>,
        photos: List<String> = emptyList()
    ) {
        Log.d("DashboardViewModel", "updateHotel - Request: id=$hotelId, name=$name, city=$city")
        launchSafe {
            val contactInfo = HotelContactInfo(
                address = address,
                phoneNumber = phone,
                email = email,
                location = "$city, $address"
            )
            val hotelDto = HotelDto(
                id = hotelId.toLong(),
                name = name,
                city = city,
                photos = photos,
                amenities = amenities,
                contactInfo = contactInfo,
                active = true
            )
            adminRepository.updateHotel(hotelId.toLong(), hotelDto).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        Log.d("DashboardViewModel", "updateHotel - Loading...")
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "updateHotel - Failed: ${result.message}")
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "updateHotel - Success! Updated Hotel ID: ${result.data.id}")
                        _isLoading.value = false
                        fetchManagerHotels()
                    }
                }
            }
        }
    }

    fun deleteHotel(hotelId: Int) {
        Log.d("DashboardViewModel", "deleteHotel - Request: id=$hotelId")
        launchSafe {
            adminRepository.deleteHotel(hotelId.toLong()).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        Log.d("DashboardViewModel", "deleteHotel - Loading...")
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "deleteHotel - Failed: ${result.message}")
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "deleteHotel - Success! Deleted Hotel ID: $hotelId")
                        _isLoading.value = false
                        fetchManagerHotels()
                    }
                }
            }
        }
    }

    fun toggleHotelStatus(hotelId: Int) {
        Log.d("DashboardViewModel", "toggleHotelStatus - Request: id=$hotelId")
        launchSafe {
            adminRepository.toggleHotelStatus(hotelId.toLong()).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        Log.d("DashboardViewModel", "toggleHotelStatus - Loading...")
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "toggleHotelStatus - Failed: ${result.message}")
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "toggleHotelStatus - Success! Toggled status for Hotel ID: $hotelId")
                        _isLoading.value = false
                        fetchManagerHotels()
                    }
                }
            }
        }
    }

    fun setSurgeFactor(hotelId: Int, surgeFactor: Double, startDate: String, endDate: String) {
        Log.d("DashboardViewModel", "setSurgeFactor - Request: id=$hotelId, surge=$surgeFactor, date=$startDate to $endDate")
        launchSafe {
            val request = SurgeUpdateDto(
                surgeFactor = BigDecimal.valueOf(surgeFactor),
                startDate = startDate,
                endDate = endDate
            )
            adminRepository.updateSurgeFactor(hotelId.toLong(), request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        Log.d("DashboardViewModel", "setSurgeFactor - Loading...")
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "setSurgeFactor - Failed: ${result.message}")
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "setSurgeFactor - Success! Set surge for Hotel ID: $hotelId")
                        _isLoading.value = false
                        fetchManagerHotels()
                    }
                }
            }
        }
    }

    // 9. Room Management CRUD
    fun createRoom(
        hotelId: Int,
        types: String,
        basePrice: Double,
        capacity: Int,
        totalCount: Int,
        amenities: List<String>,
        photos: List<String>
    ) {
        Log.d("DashboardViewModel", "createRoom - Request: hotelId=$hotelId, type=$types, basePrice=$basePrice, count=$totalCount")
        launchSafe {
            val roomDto = RoomDto(
                id = 0,
                types = types,
                basePrice = basePrice,
                photos = photos,
                amenities = amenities,
                totalCount = totalCount,
                capacity = capacity
            )
            adminRepository.createRoom(hotelId.toLong(), roomDto.toRemote()).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        Log.d("DashboardViewModel", "createRoom - Loading...")
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "createRoom - Failed: ${result.message}")
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "createRoom - Success! Created Room ID: ${result.data.id} in Hotel: $hotelId")
                        _isLoading.value = false
                        fetchRoomsForHotel(hotelId.toLong())
                    }
                }
            }
        }
    }

    fun updateRoom(
        hotelId: Int,
        roomId: Int,
        types: String,
        basePrice: Double,
        capacity: Int,
        totalCount: Int,
        amenities: List<String>,
        photos: List<String>
    ) {
        Log.d("DashboardViewModel", "updateRoom - Request: hotelId=$hotelId, roomId=$roomId, basePrice=$basePrice, count=$totalCount")
        launchSafe {
            val roomDto = RoomDto(
                id = roomId,
                types = types,
                basePrice = basePrice,
                photos = photos,
                amenities = amenities,
                totalCount = totalCount,
                capacity = capacity
            )
            adminRepository.updateRoom(hotelId.toLong(), roomId.toLong(), roomDto.toRemote()).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        Log.d("DashboardViewModel", "updateRoom - Loading...")
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "updateRoom - Failed: ${result.message}")
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "updateRoom - Success! Updated Room ID: ${result.data.id}")
                        _isLoading.value = false
                        fetchRoomsForHotel(hotelId.toLong())
                    }
                }
            }
        }
    }

    fun deleteRoom(hotelId: Int, roomId: Int) {
        Log.d("DashboardViewModel", "deleteRoom - Request: hotelId=$hotelId, roomId=$roomId")
        launchSafe {
            adminRepository.deleteRoom(hotelId.toLong(), roomId.toLong()).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        Log.d("DashboardViewModel", "deleteRoom - Loading...")
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "deleteRoom - Failed: ${result.message}")
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "deleteRoom - Success! Deleted Room ID: $roomId")
                        _isLoading.value = false
                        fetchRoomsForHotel(hotelId.toLong())
                    }
                }
            }
        }
    }

    fun fetchFavoriteHotels() {
        Log.d("DashboardViewModel", "fetchFavoriteHotels - Loading wishlisted hotels from backend")
        launchSafe {
            authRepository.getFavoriteHotels().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val ids = result.data.mapNotNull { it.id?.toInt() }.toSet()
                        Log.d("DashboardViewModel", "fetchFavoriteHotels - Loaded favorites IDs: $ids")
                        _favoriteIds.value = ids
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "fetchFavoriteHotels - Error loading favorites: ${result.message}")
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    fun toggleFavorite(hotelId: Int) {
        val currentFavs = _favoriteIds.value
        val isFav = currentFavs.contains(hotelId)
        Log.d("DashboardViewModel", "toggleFavorite - hotelId: $hotelId, currentlyFavorite: $isFav")
        
        launchSafe {
            val flow = if (isFav) {
                authRepository.removeHotelFromFavorites(hotelId.toLong())
            } else {
                authRepository.addHotelToFavorites(hotelId.toLong())
            }
            flow.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d("DashboardViewModel", "toggleFavorite - Success!")
                        if (isFav) {
                            _favoriteIds.value = currentFavs - hotelId
                        } else {
                            _favoriteIds.value = currentFavs + hotelId
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e("DashboardViewModel", "toggleFavorite - Failed: ${result.message}")
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    fun uploadImage(file: java.io.File, onResult: (String?) -> Unit) {
        launchSafe {
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestBody)
            adminRepository.uploadImage(part).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        val url = result.data?.get("url") ?: result.data?.get("data")
                        onResult(url)
                    }
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                        onResult(null)
                    }
                }
            }
        }
    }

    fun addBooking(booking: BookingDto) {
        _bookings.value = _bookings.value + booking
    }

    fun getHotelById(hotelId: Int): Hotel? {
        return _searchResults.value.find { it.hotel.id == hotelId }?.hotel
    }
}
