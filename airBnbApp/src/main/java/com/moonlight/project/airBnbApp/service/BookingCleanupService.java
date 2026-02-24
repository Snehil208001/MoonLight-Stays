package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.entity.Booking;
import com.moonlight.project.airBnbApp.entity.Inventory;
import com.moonlight.project.airBnbApp.entity.enums.BookingStatus;
import com.moonlight.project.airBnbApp.repository.BookingRepository;
import com.moonlight.project.airBnbApp.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCleanupService {

    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;

    // Runs automatically every 5 minutes
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void releaseExpiredBookings() {
        // Define the expiration threshold (10 minutes ago)
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        // Find bookings that are stuck in an incomplete state and are older than 10 minutes
        List<Booking> expiredBookings = bookingRepository.findByBookingStatusInAndCreatedAtBefore(
                List.of(BookingStatus.RESERVED, BookingStatus.GUEST_ADDED, BookingStatus.PAYMENT_PENDING),
                tenMinutesAgo
        );

        if (!expiredBookings.isEmpty()) {
            log.info("Found {} expired bookings. Releasing inventory...", expiredBookings.size());
        }

        for (Booking booking : expiredBookings) {
            // 1. Fetch and lock the specific inventory days for this booking
            List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                    booking.getRoom().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    0 // 0 means we are just locking the rows to modify them, exactly like in cancelBooking
            );

            // 2. Reduce the reserved count to free up the room for other users
            for (Inventory inventory : inventoryList) {
                inventory.setReservedCount(inventory.getReservedCount() - booking.getRoomsCount());
            }
            inventoryRepository.saveAll(inventoryList);

            // 3. Mark the abandoned booking as CANCELLED
            booking.setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            log.info("Successfully released inventory for expired booking ID: {}", booking.getId());
        }
    }
}