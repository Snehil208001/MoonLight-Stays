package com.moonlight.project.airBnbApp.service;

import com.moonlight.project.airBnbApp.dto.ReviewDto;
import java.util.List;

public interface ReviewService {
    ReviewDto addReview(Long hotelId, ReviewDto reviewDto);
    List<ReviewDto> getHotelReviews(Long hotelId);
    Double getHotelAverageRating(Long hotelId);
}