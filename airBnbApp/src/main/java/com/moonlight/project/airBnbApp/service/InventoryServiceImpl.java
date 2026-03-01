package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.HotelPriceDto;
import com.moonlight.project.airBnbApp.dto.HotelSearchRequest;
import com.moonlight.project.airBnbApp.dto.RoomPriceDto;
import com.moonlight.project.airBnbApp.entity.Hotel;
import com.moonlight.project.airBnbApp.entity.Inventory;
import com.moonlight.project.airBnbApp.entity.Room;
import com.moonlight.project.airBnbApp.exception.ResourceNotFoundException;
import com.moonlight.project.airBnbApp.repository.HotelMinPriceRepository;
import com.moonlight.project.airBnbApp.repository.HotelRepository;
import com.moonlight.project.airBnbApp.repository.InventoryRepository;
import com.moonlight.project.airBnbApp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        List<Inventory> inventoryList = new ArrayList<>();

        for (; today.isBefore(endDate); today = today.plusDays(1)){
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryList.add(inventory);
        }

        inventoryRepository.saveAll(inventoryList);
    }

    @Override
    @Transactional
    public void deleteFutureInventories(Room room) {
        LocalDate today = LocalDate.now();
        inventoryRepository.deleteByDateAfterAndRoom(today,room);
    }

    @Override
    @Transactional
    public void deleteAllInventories(Room room) {
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        int page = hotelSearchRequest.getPage() != null ? hotelSearchRequest.getPage() : 0;
        int size = hotelSearchRequest.getSize() != null && hotelSearchRequest.getSize() > 0 ? hotelSearchRequest.getSize() : 10;
        // Clamp to prevent "Page offset exceeds Integer.MAX_VALUE" when page/size are huge (e.g. from Swagger)
        if (page < 0) page = 0;
        if (size > 100) size = 100;
        int maxPage = size > 0 ? Integer.MAX_VALUE / size : 0;
        if (page > maxPage) page = maxPage;
        Pageable pageable = PageRequest.of(page, size);

        LocalDate checkIn = hotelSearchRequest.getCheckInDate();
        LocalDate endDate = hotelSearchRequest.getEndDate();
        if (checkIn == null) checkIn = LocalDate.now();
        if (endDate == null) endDate = checkIn.plusDays(1);
        if (endDate.isBefore(checkIn)) endDate = checkIn.plusDays(1);

        long dateCount = ChronoUnit.DAYS.between(checkIn, endDate);
        if (dateCount < 1) dateCount = 1; // Minimum 1 night
        LocalDate endDateExclusive = checkIn.plusDays(dateCount);
        LocalDate endDateInclusive = endDateExclusive.minusDays(1);

        String city = hotelSearchRequest.getCity();
        boolean searchAllCities = city == null || city.isBlank();

        String roomType = (hotelSearchRequest.getRoomType() != null && !hotelSearchRequest.getRoomType().isBlank())
                ? hotelSearchRequest.getRoomType().trim() : null;
        String amenity = (hotelSearchRequest.getAmenity() != null && !hotelSearchRequest.getAmenity().isBlank())
                ? hotelSearchRequest.getAmenity().trim() : null;
        Integer roomsCount = (hotelSearchRequest.getRoomsCount() != null && hotelSearchRequest.getRoomsCount() > 0)
                ? hotelSearchRequest.getRoomsCount() : 1;

        Page<HotelPriceDto> hotelPage = searchAllCities
                ? hotelMinPriceRepository.findHotelsWithAvailableInventoryAllCities(
                        checkIn,
                        endDateInclusive,
                        roomsCount,
                        (int) dateCount,
                        hotelSearchRequest.getMinPrice(),
                        hotelSearchRequest.getMaxPrice(),
                        roomType,
                        amenity,
                        pageable)
                : hotelMinPriceRepository.findHotelsWithAvailableInventory(
                        city.trim(),
                        checkIn,
                        endDateInclusive,
                        roomsCount,
                        (int) dateCount,
                        hotelSearchRequest.getMinPrice(),
                        hotelSearchRequest.getMaxPrice(),
                        roomType,
                        amenity,
                        pageable);

        return hotelPage;
    }

    @Override
    public List<RoomPriceDto> getRoomPricesForHotel(Long hotelId, LocalDate checkIn, LocalDate checkOut, Integer roomsCount) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));
        final long nights = Math.max(1, ChronoUnit.DAYS.between(checkIn, checkOut));
        LocalDate endDateExclusive = checkIn.plusDays(nights);
        final int count = roomsCount != null && roomsCount > 0 ? roomsCount : 1;

        return hotel.getRooms().stream()
                .map(room -> {
                    List<Inventory> inventories = inventoryRepository.findByRoomAndDateRangeForPricing(
                            room.getId(), checkIn, endDateExclusive, count);
                    if (inventories.size() != nights) {
                        return null; // not available for all nights
                    }
                    BigDecimal total = inventories.stream()
                            .map(inv -> inv.getPrice().multiply(BigDecimal.valueOf(count)))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal pricePerNight = total.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);
                    return new RoomPriceDto(room.getId(), pricePerNight, total);
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void appendNewInventoryDay() {
        LocalDate targetDate = LocalDate.now().plusYears(1).minusDays(1);

        List<Room> allRooms = roomRepository.findAll();
        List<Inventory> newInventories = new ArrayList<>();

        for (Room room : allRooms) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(targetDate)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            newInventories.add(inventory);
        }

        inventoryRepository.saveAll(newInventories);
    }
}