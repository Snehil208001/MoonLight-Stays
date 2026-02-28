package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.RoomDto;
import com.moonlight.project.airBnbApp.entity.Hotel;
import com.moonlight.project.airBnbApp.entity.Room;
import com.moonlight.project.airBnbApp.entity.User;
import com.moonlight.project.airBnbApp.exception.ResourceNotFoundException;
import com.moonlight.project.airBnbApp.exception.UnAuthorisedExceptions;
import com.moonlight.project.airBnbApp.repository.HotelRepository;
import com.moonlight.project.airBnbApp.repository.RoomRepository;
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
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final PricingUpdateService pricingUpdateService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ hotelId));

        // Check Ownership before allowing room creation
        if (!hotel.getOwner().getId().equals(getCurrentUser().getId())) {
            throw new UnAuthorisedExceptions("You do not own this hotel and cannot add rooms to it.");
        }

        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        inventoryService.initializeRoomForAYear(room);
        pricingUpdateService.updateHotelPricesForHotel(hotel); // Populate HotelMinPrice so hotel appears in search

        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    @Transactional
    public RoomDto updateRoom(Long hotelId, Long roomId, RoomDto roomDto) {
        log.info("Updating room with ID: {} in hotel with ID: {}", roomId, hotelId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new ResourceNotFoundException("Room does not belong to this hotel.");
        }

        if (!room.getHotel().getOwner().getId().equals(getCurrentUser().getId())) {
            throw new UnAuthorisedExceptions("You do not own this hotel and cannot update rooms.");
        }

        if (roomDto.getTypes() != null) room.setTypes(roomDto.getTypes());
        if (roomDto.getBasePrice() != null) room.setBasePrice(roomDto.getBasePrice());
        if (roomDto.getPhotos() != null) room.setPhotos(roomDto.getPhotos());
        if (roomDto.getAmenities() != null) room.setAmenities(roomDto.getAmenities());
        if (roomDto.getTotalCount() != null) room.setTotalCount(roomDto.getTotalCount());
        if (roomDto.getCapacity() != null) room.setCapacity(roomDto.getCapacity());

        room = roomRepository.save(room);

        // Update pricing in background - use fresh hotel fetch to avoid lazy/detached entity issues
        Long hotelIdForPricing = room.getHotel() != null ? room.getHotel().getId() : hotelId;
        try {
            Hotel hotelForPricing = hotelRepository.findById(hotelIdForPricing).orElse(null);
            if (hotelForPricing != null) {
                pricingUpdateService.updateHotelPricesForHotel(hotelForPricing);
            }
        } catch (Exception e) {
            log.warn("Failed to update hotel prices after room update (room saved successfully): {}", e.getMessage());
        }

        return toRoomDto(room);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting  all rooms in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+ hotelId));
        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element,RoomDto.class)).collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+ roomId));
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+ roomId));

        // Check Ownership before allowing room deletion
        if (!room.getHotel().getOwner().getId().equals(getCurrentUser().getId())) {
            throw new UnAuthorisedExceptions("You do not own the hotel this room belongs to.");
        }

        // 1. Delete ALL inventories first (Children)
        inventoryService.deleteAllInventories(room);

        // 2. Then delete the room (Parent)
        roomRepository.deleteById(roomId);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private RoomDto toRoomDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setTypes(room.getTypes());
        dto.setBasePrice(room.getBasePrice());
        dto.setPhotos(room.getPhotos());
        dto.setAmenities(room.getAmenities());
        dto.setTotalCount(room.getTotalCount());
        dto.setCapacity(room.getCapacity());
        return dto;
    }
}