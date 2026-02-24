package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.BookingDto;
import com.moonlight.project.airBnbApp.dto.BookingRequest;
import com.moonlight.project.airBnbApp.dto.GuestDto;

import java.util.List;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    BookingDto getBookingById(Long bookingId);

    List<BookingDto> getMyBookings();

    void cancelBooking(Long bookingId);

    String initiatePayments(Long bookingId);

    // Method to capture successful payment from webhook
    void capturePayment(String sessionId);
}