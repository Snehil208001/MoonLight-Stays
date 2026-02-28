package com.moonlight.project.airBnbApp.controller;

import com.moonlight.project.airBnbApp.dto.ReviewDto;
import com.moonlight.project.airBnbApp.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels/{hotelId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> addReview(@PathVariable Long hotelId, @RequestBody ReviewDto reviewDto) {
        return new ResponseEntity<>(reviewService.addReview(hotelId, reviewDto), HttpStatus.CREATED);
    }

    // --- UPDATED: To support Pagination ---
    @GetMapping
    public ResponseEntity<Page<ReviewDto>> getHotelReviews(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getHotelReviewsPaginated(hotelId, page, size));
    }

    @GetMapping("/average")
    public ResponseEntity<Double> getHotelAverageRating(@PathVariable Long hotelId) {
        return ResponseEntity.ok(reviewService.getHotelAverageRating(hotelId));
    }
}