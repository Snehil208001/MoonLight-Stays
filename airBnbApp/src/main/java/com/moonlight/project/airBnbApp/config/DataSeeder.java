package com.moonlight.project.airBnbApp.config;

import com.moonlight.project.airBnbApp.entity.Hotel;
import com.moonlight.project.airBnbApp.entity.Room;
import com.moonlight.project.airBnbApp.entity.User;
import com.moonlight.project.airBnbApp.entity.enums.Role;
import com.moonlight.project.airBnbApp.repository.HotelRepository;
import com.moonlight.project.airBnbApp.repository.RoomRepository;
import com.moonlight.project.airBnbApp.repository.UserRepository;
import com.moonlight.project.airBnbApp.service.InventoryService;
import com.moonlight.project.airBnbApp.service.PricingUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InventoryService inventoryService;
    private final PricingUpdateService pricingUpdateService;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    private static final int MIN_HOTELS_TO_SEED = 8;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) return;

        long count = hotelRepository.count();
        if (count >= MIN_HOTELS_TO_SEED) {
            log.info("Database has {} hotels, skipping seed", count);
            return;
        }

        log.info("Seeding sample data (current: {}, target: {})...", count, MIN_HOTELS_TO_SEED);
        User manager = getOrCreateHotelManager();

        List<HotelData> allHotels = List.of(
                new HotelData("Taj Palace", "Mumbai", new String[]{"Pool", "Spa", "Free WiFi", "Restaurant"}),
                new HotelData("The Oberoi", "Mumbai", new String[]{"Pool", "Gym", "Free WiFi", "Bar"}),
                new HotelData("ITC Maurya", "Delhi", new String[]{"Pool", "Spa", "Free WiFi", "Restaurant"}),
                new HotelData("The Leela Palace", "Bangalore", new String[]{"Pool", "Gym", "Free WiFi"}),
                new HotelData("Grand Hyatt", "Mumbai", new String[]{"Pool", "Spa", "Gym", "Free WiFi"}),
                new HotelData("Radisson Blu", "Delhi", new String[]{"Pool", "Free WiFi", "Restaurant"}),
                new HotelData("Park Plaza", "Bangalore", new String[]{"Gym", "Free WiFi", "Bar"}),
                new HotelData("Hilton Garden Inn", "Chennai", new String[]{"Pool", "Free WiFi", "Restaurant"})
        );

        int toAdd = (int) Math.min(MIN_HOTELS_TO_SEED - count, allHotels.size());
        List<HotelData> hotelsToAdd = allHotels.subList(0, toAdd);
        seedHotelsWithRooms(manager, hotelsToAdd);
    }

    private void seedHotelsWithRooms(User manager, List<HotelData> hotels) {
        for (HotelData hd : hotels) {
            Hotel hotel = new Hotel();
            hotel.setName(hd.name);
            hotel.setCity(hd.city);
            hotel.setAmenities(hd.amenities);
            hotel.setOwner(manager);
            hotel.setActive(true);
            hotel = hotelRepository.save(hotel);

            Room room = new Room();
            room.setHotel(hotel);
            room.setTypes("Deluxe");
            room.setBasePrice(new BigDecimal("2500"));
            room.setTotalCount(5);
            room.setCapacity(2);
            room = roomRepository.save(room);
            inventoryService.initializeRoomForAYear(room);
            pricingUpdateService.updateHotelPricesForHotel(hotel);
        }
        log.info("Seeded {} sample hotels", hotels.size());
    }

    private User getOrCreateHotelManager() {
        Optional<User> existing = userRepository.findByEmail("manager@moonlight.com");
        if (existing.isPresent()) return existing.get();

        User manager = new User();
        manager.setEmail("manager@moonlight.com");
        manager.setName("Hotel Manager");
        manager.setPassword(passwordEncoder.encode("manager123"));
        manager.setRoles(Set.of(Role.HOTEL_MANAGER));
        return userRepository.save(manager);
    }

    private record HotelData(String name, String city, String[] amenities) {}
}
