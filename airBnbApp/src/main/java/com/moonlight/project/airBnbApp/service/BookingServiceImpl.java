package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.BookingDto;
import com.moonlight.project.airBnbApp.dto.BookingRequest;
import com.moonlight.project.airBnbApp.dto.GuestDto;
import com.moonlight.project.airBnbApp.entity.*;
import com.moonlight.project.airBnbApp.entity.enums.BookingStatus;
import com.moonlight.project.airBnbApp.exception.ResourceNotFoundException;
import com.moonlight.project.airBnbApp.exception.UnAuthorisedExceptions;
import com.moonlight.project.airBnbApp.repository.*;
import com.moonlight.project.airBnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;
    private final EmailService emailService;

    // --- NEW: Added Repo for Promo Codes ---
    private final PromoCodeRepository promoCodeRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {
        log.info("Initialising booking for hotel : {}, room: {}, dates: {} to {}", bookingRequest.getHotelId(),
                bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(() ->
                new ResourceNotFoundException("Hotel not found with id: " + bookingRequest.getHotelId()));

        if (!hotel.getActive()) {
            throw new IllegalStateException("Hotel is currently inactive and cannot be booked.");
        }

        Room room = roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(() ->
                new ResourceNotFoundException("Room not found with id: " + bookingRequest.getRoomId()));

        if (!room.getHotel().getId().equals(hotel.getId())) {
            throw new IllegalStateException("The requested room does not belong to the requested hotel.");
        }

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(room.getId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        if (inventoryList.size() != daysCount) {
            throw new IllegalStateException("Room is not available anymore");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Inventory inventory : inventoryList) {
            BigDecimal dailyPrice = pricingService.calculateDynamicPricing(inventory);
            BigDecimal priceForRequestedRooms = dailyPrice.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));
            totalAmount = totalAmount.add(priceForRequestedRooms);
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
        }

        // --- NEW: Apply Promo Code Math ---
        if (bookingRequest.getPromoCode() != null && !bookingRequest.getPromoCode().trim().isEmpty()) {
            PromoCode promoCode = promoCodeRepository.findByCodeAndActiveTrue(bookingRequest.getPromoCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired promo code: " + bookingRequest.getPromoCode()));

            BigDecimal discountPercentage = BigDecimal.valueOf(promoCode.getDiscountPercentage() / 100.0);
            BigDecimal discountAmount = totalAmount.multiply(discountPercentage);
            totalAmount = totalAmount.subtract(discountAmount);

            log.info("Applied promo code {}: Discounted amount by {}", promoCode.getCode(), discountAmount);
        }

        inventoryRepository.saveAll(inventoryList);

        User user = getCurrentUser();

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(user)
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalAmount)
                .promoCode(bookingRequest.getPromoCode()) // --- NEW: Save applied code ---
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding Guests for booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with id: " + bookingId));

        checkBookingOwnership(booking);

        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        if (booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
        }

        for (GuestDto guestDto: guestDtoList) {
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(booking.getUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    public BookingDto getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with id: " + bookingId));

        checkBookingOwnership(booking);

        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();
        return bookingRepository.findByUser(user)
                .stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<BookingDto> getMyBookingsPaginated(int page, int size, List<BookingStatus> statusFilter) {
        User user = getCurrentUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (statusFilter != null && !statusFilter.isEmpty()) {
            return bookingRepository.findByUserAndBookingStatusIn(user, statusFilter, pageRequest)
                    .map(booking -> modelMapper.map(booking, BookingDto.class));
        }
        return bookingRepository.findByUser(user, pageRequest)
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with id: " + bookingId));

        checkBookingOwnership(booking);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        boolean isRefunded = false;

        if (booking.getBookingStatus() == BookingStatus.CONFIRMED && booking.getPaymentSessionId() != null) {
            log.info("Booking is CONFIRMED. Initiating Stripe refund for session: {}", booking.getPaymentSessionId());
            checkoutService.refundPayment(booking.getPaymentSessionId());
            isRefunded = true;
        }

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                0
        );

        for (Inventory inventory : inventoryList) {
            inventory.setReservedCount(inventory.getReservedCount() - booking.getRoomsCount());
        }

        inventoryRepository.saveAll(inventoryList);

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking {} has been successfully cancelled.", bookingId);

        emailService.sendBookingCancellationEmail(
                booking.getUser().getEmail(),
                booking.getUser().getName(),
                booking.getId(),
                booking.getHotel().getName(),
                isRefunded
        );
    }

    @Override
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->
                new ResourceNotFoundException("Booking not found with id: " + bookingId));

        checkBookingOwnership(booking);

        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,
                frontendUrl+"/payments/success",frontendUrl+"/payments/failure");


        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(String sessionId) {
        capturePayment(sessionId, null);
    }

    @Override
    @Transactional
    public void capturePayment(String sessionId, String clientReferenceId) {
        Booking booking = bookingRepository.findByPaymentSessionId(sessionId)
                .or(() -> {
                    if (clientReferenceId != null && !clientReferenceId.isBlank()) {
                        try {
                            Long bookingId = Long.parseLong(clientReferenceId.trim());
                            return bookingRepository.findById(bookingId);
                        } catch (NumberFormatException ignored) {
                            return Optional.empty();
                        }
                    }
                    return Optional.empty();
                })
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found for session ID: " + sessionId +
                                (clientReferenceId != null ? ", client_reference_id: " + clientReferenceId : "")));

        if (booking.getPaymentSessionId() == null) {
            booking.setPaymentSessionId(sessionId);
            bookingRepository.save(booking);
        }

        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            log.info("Payment for session {} was already captured. Skipping DB update and email.", sessionId);
            return;
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        log.info("Successfully captured payment and confirmed booking ID: {}", booking.getId());

        emailService.sendBookingConfirmation(
                booking.getUser().getEmail(),
                booking.getUser().getName(),
                booking.getId(),
                booking.getHotel().getName(),
                booking.getCheckInDate().toString(),
                booking.getCheckOutDate().toString()
        );
    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void checkBookingOwnership(Booking booking) {
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(booking.getUser().getId())) {
            throw new UnAuthorisedExceptions("You do not have permission to access or modify this booking.");
        }
    }
}