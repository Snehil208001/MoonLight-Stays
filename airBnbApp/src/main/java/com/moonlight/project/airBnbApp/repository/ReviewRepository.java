package com.moonlight.project.airBnbApp.repository;

import com.moonlight.project.airBnbApp.entity.Hotel;
import com.moonlight.project.airBnbApp.entity.Review;
import com.moonlight.project.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByHotelId(Long hotelId);

    // Prevent multiple reviews from the same user for the same hotel
    boolean existsByUserAndHotel(User user, Hotel hotel);

    // Calculate the average rating for a hotel
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotel.id = :hotelId")
    Double getAverageRatingForHotel(@Param("hotelId") Long hotelId);
}