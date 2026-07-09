package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.ReviewDto;
import com.moonlight.project.airBnbApp.entity.Hotel;
import com.moonlight.project.airBnbApp.entity.Review;
import com.moonlight.project.airBnbApp.entity.User;
import com.moonlight.project.airBnbApp.entity.enums.BookingStatus;
import com.moonlight.project.airBnbApp.exception.ResourceNotFoundException;
import com.moonlight.project.airBnbApp.repository.BookingRepository;
import com.moonlight.project.airBnbApp.repository.HotelRepository;
import com.moonlight.project.airBnbApp.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ReviewDto addReview(Long hotelId, ReviewDto reviewDto) {
        log.info("Attempting to add a review for hotel ID: {}", hotelId);
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        // 1. Core Rule: Must have a CONFIRMED booking where check-out date is today or has passed
        List<com.moonlight.project.airBnbApp.entity.Booking> userBookings = bookingRepository.findByUserAndHotelAndBookingStatus(currentUser, hotel, BookingStatus.CONFIRMED);
        boolean hasCompletedStay = userBookings.stream()
                .anyMatch(booking -> {
                    java.time.LocalDate checkout = booking.getCheckOutDate();
                    java.time.LocalDate today = java.time.LocalDate.now();
                    return checkout.isBefore(today) || checkout.isEqual(today);
                });
        if (!hasCompletedStay) {
            throw new IllegalStateException("You can only review hotels where you have a confirmed booking and the checkout date has passed.");
        }

        // 2. Core Rule: One review per user per hotel
        if (reviewRepository.existsByUserAndHotel(currentUser, hotel)) {
            throw new IllegalStateException("You have already reviewed this hotel.");
        }

        // Validate rating bounds
        if (reviewDto.getRating() < 1 || reviewDto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }

        Review review = modelMapper.map(reviewDto, Review.class);
        review.setHotel(hotel);
        review.setUser(currentUser);

        review = reviewRepository.save(review);
        log.info("Successfully added review ID: {} for hotel ID: {}", review.getId(), hotelId);

        return modelMapper.map(review, ReviewDto.class);
    }

    @Override
    public List<ReviewDto> getHotelReviews(Long hotelId) {
        return reviewRepository.findByHotelId(hotelId)
                .stream()
                .map(review -> modelMapper.map(review, ReviewDto.class))
                .collect(Collectors.toList());
    }

    // --- ADDED: Paginated method ---
    @Override
    public Page<ReviewDto> getHotelReviewsPaginated(Long hotelId, int page, int size) {
        return reviewRepository.findByHotelId(hotelId, PageRequest.of(page, size))
                .map(review -> modelMapper.map(review, ReviewDto.class));
    }

    @Override
    public Double getHotelAverageRating(Long hotelId) {
        Double average = reviewRepository.getAverageRatingForHotel(hotelId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0; // Returns rounded value like 4.5
    }
}