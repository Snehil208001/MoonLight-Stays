package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.HotelDto;
import com.moonlight.project.airBnbApp.dto.ProfileUpdateDto;
import com.moonlight.project.airBnbApp.dto.UserDto;
import com.moonlight.project.airBnbApp.entity.User;

import java.util.List;

public interface UserService {

    User getUserById(Long id);

    // --- NEW METHODS ---
    UserDto getMyProfile();

    UserDto updateProfile(ProfileUpdateDto profileUpdateDto);

    void addHotelToFavorites(Long hotelId);

    void removeHotelFromFavorites(Long hotelId);

    List<HotelDto> getFavoriteHotels();
}