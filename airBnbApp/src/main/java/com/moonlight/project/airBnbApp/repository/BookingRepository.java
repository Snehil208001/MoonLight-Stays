package com.moonlight.project.airBnbApp.repository;

import com.moonlight.project.airBnbApp.entity.Booking;
import com.moonlight.project.airBnbApp.entity.Hotel;
import com.moonlight.project.airBnbApp.entity.User;
import com.moonlight.project.airBnbApp.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.hotel.id = :hotelId")
    void deleteByHotelId(@Param("hotelId") Long hotelId);

    List<Booking> findByUser(User user);

    Page<Booking> findByUser(User user, Pageable pageable);

    Page<Booking> findByUserAndBookingStatusIn(User user, List<BookingStatus> statuses, Pageable pageable);

    Optional<Booking> findByPaymentSessionId(String paymentSessionId);

    List<Booking> findByBookingStatusInAndCreatedAtBefore(List<BookingStatus> statuses, LocalDateTime createdAt);

    boolean existsByUserAndHotelAndBookingStatus(User user, Hotel hotel, BookingStatus status);
}