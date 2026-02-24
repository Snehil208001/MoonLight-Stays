package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.HotelDto;
import com.moonlight.project.airBnbApp.dto.ProfileUpdateDto;
import com.moonlight.project.airBnbApp.dto.UserDto;
import com.moonlight.project.airBnbApp.entity.Hotel;
import com.moonlight.project.airBnbApp.entity.User;
import com.moonlight.project.airBnbApp.exception.ResourceNotFoundException;
import com.moonlight.project.airBnbApp.repository.HotelRepository;
import com.moonlight.project.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // Helper method to get the fresh logged-in user from the database
    private User getCurrentUser() {
        User securityUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(securityUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserDto getMyProfile() {
        User user = getCurrentUser();
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto updateProfile(ProfileUpdateDto profileUpdateDto) {
        User user = getCurrentUser();

        if (profileUpdateDto.getName() != null) {
            user.setName(profileUpdateDto.getName());
        }

        user = userRepository.save(user);
        log.info("User {} successfully updated their profile.", user.getId());
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public void addHotelToFavorites(Long hotelId) {
        User user = getCurrentUser();
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        user.getFavoriteHotels().add(hotel);
        userRepository.save(user);
        log.info("Hotel ID {} added to favorites for User ID {}", hotelId, user.getId());
    }

    @Override
    @Transactional
    public void removeHotelFromFavorites(Long hotelId) {
        User user = getCurrentUser();
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        user.getFavoriteHotels().remove(hotel);
        userRepository.save(user);
        log.info("Hotel ID {} removed from favorites for User ID {}", hotelId, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelDto> getFavoriteHotels() {
        User user = getCurrentUser();
        return user.getFavoriteHotels().stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class))
                .collect(Collectors.toList());
    }
}