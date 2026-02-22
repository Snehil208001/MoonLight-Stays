package com.moonlight.project.airBnbApp.repository;

import com.moonlight.project.airBnbApp.entity.Booking;
import com.moonlight.project.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Add this to fetch bookings by the user
    List<Booking> findByUser(User user);
}