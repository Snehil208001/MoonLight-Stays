package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.HotelDto;
import com.moonlight.project.airBnbApp.dto.HotelInfoDto;
import com.moonlight.project.airBnbApp.dto.RoomDto;
import com.moonlight.project.airBnbApp.entity.Hotel;
import com.moonlight.project.airBnbApp.entity.Inventory;
import com.moonlight.project.airBnbApp.entity.Room;
import com.moonlight.project.airBnbApp.entity.User;
import com.moonlight.project.airBnbApp.exception.ResourceNotFoundException;
import com.moonlight.project.airBnbApp.exception.UnAuthorisedExceptions;
import com.moonlight.project.airBnbApp.repository.HotelRepository;
import com.moonlight.project.airBnbApp.repository.InventoryRepository;
import com.moonlight.project.airBnbApp.repository.RoomRepository;
import com.moonlight.project.airBnbApp.strategy.PricingService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    // NEW: Inject InventoryRepository and PricingService
    private final InventoryRepository inventoryRepository;
    private final PricingService pricingService;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("creating a new hotel with name: {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);

        User currentUser = getCurrentUser();
        hotel.setOwner(currentUser);

        hotel = hotelRepository.save(hotel);
        log.info("created a new hotel with ID: {}", hotelDto.getId());
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        log.info("Getting all hotels");
        return hotelRepository.findAll()
                .stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting the hotel with ID: {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ id));
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating the hotel with ID: {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ id));

        checkOwnership(hotel);

        hotelDto.setId(id);

        User owner = hotel.getOwner();
        Boolean isActive = hotel.getActive();

        modelMapper.map(hotelDto, hotel);

        hotel.setOwner(owner);
        hotel.setActive(isActive);

        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ id));

        checkOwnership(hotel);

        for (Room room: hotel.getRooms()) {
            inventoryService.deleteAllInventories(room);
            roomRepository.delete(room);
        }

        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Toggling activation status for hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ hotelId));

        checkOwnership(hotel);

        if (!hotel.getActive() && (hotel.getRooms() == null || hotel.getRooms().isEmpty())) {
            throw new IllegalStateException("Cannot activate a hotel that has no rooms. Please add rooms first.");
        }

        hotel.setActive(!hotel.getActive());
        hotelRepository.save(hotel);
        log.info("Hotel ID: {} is now active: {}", hotelId, hotel.getActive());
    }

    @Override
    @Transactional
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ hotelId));
        List<RoomDto> room = hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .toList();

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), room);
    }

    // --- NEW: Surge Factor Update Logic ---
    @Override
    @Transactional
    public void updateSurgeFactor(Long hotelId, BigDecimal surgeFactor, LocalDate startDate, LocalDate endDate) {
        log.info("Updating surge factor to {} for hotel ID: {} between {} and {}", surgeFactor, hotelId, startDate, endDate);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        checkOwnership(hotel);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);

        for (Inventory inventory : inventoryList) {
            // 1. Set the new multiplier
            inventory.setSurgeFactor(surgeFactor);
            // 2. Instantly recalculate the dynamic price so the next user checking out sees the new price
            inventory.setPrice(pricingService.calculateDynamicPricing(inventory));
        }

        inventoryRepository.saveAll(inventoryList);
        log.info("Successfully updated surge factor and prices for {} inventory records.", inventoryList.size());
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void checkOwnership(Hotel hotel) {
        User currentUser = getCurrentUser();
        if (hotel.getOwner() == null || !hotel.getOwner().getId().equals(currentUser.getId())) {
            throw new UnAuthorisedExceptions("You do not have permission to modify this hotel as you are not the owner.");
        }
    }
}