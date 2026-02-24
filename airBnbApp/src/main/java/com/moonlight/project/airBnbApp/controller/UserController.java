package com.moonlight.project.airBnbApp.controller;

import com.moonlight.project.airBnbApp.dto.HotelDto;
import com.moonlight.project.airBnbApp.dto.ProfileUpdateDto;
import com.moonlight.project.airBnbApp.dto.UserDto;
import com.moonlight.project.airBnbApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(@RequestBody ProfileUpdateDto profileUpdateDto) {
        return ResponseEntity.ok(userService.updateProfile(profileUpdateDto));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<HotelDto>> getFavoriteHotels() {
        return ResponseEntity.ok(userService.getFavoriteHotels());
    }

    @PostMapping("/favorites/{hotelId}")
    public ResponseEntity<Void> addHotelToFavorites(@PathVariable Long hotelId) {
        userService.addHotelToFavorites(hotelId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/favorites/{hotelId}")
    public ResponseEntity<Void> removeHotelFromFavorites(@PathVariable Long hotelId) {
        userService.removeHotelFromFavorites(hotelId);
        return ResponseEntity.noContent().build();
    }
}