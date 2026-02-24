package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.HotelPriceDto;
import com.moonlight.project.airBnbApp.dto.HotelSearchRequest;
import com.moonlight.project.airBnbApp.entity.Inventory;
import com.moonlight.project.airBnbApp.entity.Room;
import com.moonlight.project.airBnbApp.repository.HotelMinPriceRepository;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository; // Added to fetch all rooms for cron job

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
        log.info("Deleting all inventories for room with ID: {}", room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching hotels for {} city, from {} to {}", hotelSearchRequest.getCity(),hotelSearchRequest.getCheckInDate(),hotelSearchRequest.getEndDate());
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());

        long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getCheckInDate(), hotelSearchRequest.getEndDate());

        Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(
                hotelSearchRequest.getCity(),
                hotelSearchRequest.getCheckInDate(),
                hotelSearchRequest.getEndDate().minusDays(1),
                hotelSearchRequest.getRoomsCount(),
                (int) dateCount,
                hotelSearchRequest.getMinPrice(), // NEW: Passing min price
                hotelSearchRequest.getMaxPrice(), // NEW: Passing max price
                pageable
        );

        return hotelPage;
    }

    // FIX: Daily cron job to append the 365th day so inventory never runs out
    @Scheduled(cron = "0 0 0 * * *") // Runs at midnight every day
    @Transactional
    public void appendNewInventoryDay() {
        log.info("Running daily cron job to append new inventory day for all rooms.");
        LocalDate targetDate = LocalDate.now().plusYears(1).minusDays(1); // The exact 365th day from today

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
        log.info("Successfully appended {} new inventory records for {}", newInventories.size(), targetDate);
    }
}