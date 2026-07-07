package com.moonlight.stays.features.booking.controller;

import com.moonlight.stays.exception.MoonlightException;
import com.moonlight.stays.features.booking.model.Booking;
import com.moonlight.stays.features.booking.repository.BookingRepository;
import com.moonlight.stays.features.property.model.Property;
import com.moonlight.stays.features.property.repository.PropertyRepository;
import com.moonlight.stays.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings Manager")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;

    @GetMapping
    public ResponseEntity<List<Booking>> getMyBookings(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bookingRepository.findByGuestId(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestBody Booking booking,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Property property = propertyRepository.findById(booking.getProperty().getId())
                .orElseThrow(() -> new MoonlightException("Property not found", HttpStatus.NOT_FOUND));

        booking.setProperty(property);
        booking.setGuest(principal.getUser());
        booking.setStatus("CONFIRMED"); // Default status

        Booking saved = bookingRepository.save(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new MoonlightException("Booking not found", HttpStatus.NOT_FOUND));

        if (!booking.getGuest().getId().equals(principal.getId()) && 
            !booking.getProperty().getHost().getId().equals(principal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(booking);
    }
}
