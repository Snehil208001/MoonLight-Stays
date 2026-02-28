package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.BookingDto;
import com.moonlight.project.airBnbApp.dto.BookingRequest;
import com.moonlight.project.airBnbApp.dto.GuestDto;
import com.moonlight.project.airBnbApp.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    BookingDto getBookingById(Long bookingId);

    List<BookingDto> getMyBookings();

    Page<BookingDto> getMyBookingsPaginated(int page, int size, List<BookingStatus> statusFilter);

    void cancelBooking(Long bookingId);

    String initiatePayments(Long bookingId);

    void capturePayment(String sessionId);

    void capturePayment(String sessionId, String clientReferenceId);
}