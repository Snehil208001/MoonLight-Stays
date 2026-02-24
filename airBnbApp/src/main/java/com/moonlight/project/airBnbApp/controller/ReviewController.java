package com.moonlight.project.airBnbApp.controller;

import com.moonlight.project.airBnbApp.dto.ReviewDto;
import com.moonlight.project.airBnbApp.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels/{hotelId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Secure endpoint: Only users with a JWT and confirmed booking can post
    @PostMapping
    public ResponseEntity<ReviewDto> addReview(@PathVariable Long hotelId, @RequestBody ReviewDto reviewDto) {
        return new ResponseEntity<>(reviewService.addReview(hotelId, reviewDto), HttpStatus.CREATED);
    }

    // Public endpoint: Anyone browsing the app can read reviews
    @GetMapping
    public ResponseEntity<List<ReviewDto>> getHotelReviews(@PathVariable Long hotelId) {
        return ResponseEntity.ok(reviewService.getHotelReviews(hotelId));
    }

    // Public endpoint: Get the average star rating
    @GetMapping("/average")
    public ResponseEntity<Double> getHotelAverageRating(@PathVariable Long hotelId) {
        return ResponseEntity.ok(reviewService.getHotelAverageRating(hotelId));
    }
}