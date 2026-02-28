package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.HotelPriceDto;
import com.moonlight.project.airBnbApp.dto.HotelSearchRequest;
import com.moonlight.project.airBnbApp.dto.RoomPriceDto;
import com.moonlight.project.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteFutureInventories(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    /** Get per-room dynamic prices for a hotel for given dates */
    List<RoomPriceDto> getRoomPricesForHotel(Long hotelId, LocalDate checkIn, LocalDate checkOut, Integer roomsCount);
}