package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.ReviewDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReviewService {
    ReviewDto addReview(Long hotelId, ReviewDto reviewDto);
    List<ReviewDto> getHotelReviews(Long hotelId);
    // ADDED: Paginated method
    Page<ReviewDto> getHotelReviewsPaginated(Long hotelId, int page, int size);
    Double getHotelAverageRating(Long hotelId);
}