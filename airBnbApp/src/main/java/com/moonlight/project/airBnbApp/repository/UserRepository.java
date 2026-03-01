package com.moonlight.project.airBnbApp.repository;

import com.moonlight.project.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.favoriteHotels h WHERE h.id = :hotelId")
    List<User> findUsersWithHotelInFavorites(@Param("hotelId") Long hotelId);
}