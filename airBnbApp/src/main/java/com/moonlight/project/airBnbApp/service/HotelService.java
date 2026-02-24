package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.HotelDto;
import com.moonlight.project.airBnbApp.dto.HotelInfoDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface HotelService {

    HotelDto createNewHotel(HotelDto hotelDto);

    List<HotelDto> getAllHotels();

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id, HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId);

    // NEW: Method to manually update surge pricing
    void updateSurgeFactor(Long hotelId, BigDecimal surgeFactor, LocalDate startDate, LocalDate endDate);
}