package com.moonlight.project.airBnbApp.repository;

import com.moonlight.project.airBnbApp.dto.HotelPriceDto;
import com.moonlight.project.airBnbApp.entity.Hotel;
import com.moonlight.project.airBnbApp.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {

    @Query("""
            SELECT NEW com.moonlight.project.airBnbApp.dto.HotelPriceDto(i.hotel, AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND i.hotel.active = true
            GROUP BY i.hotel
            HAVING COUNT(i.date) = :dateCount
                AND (:minPrice IS NULL OR AVG(i.price) >= :minPrice)
                AND (:maxPrice IS NULL OR AVG(i.price) <= :maxPrice)
            """)
    Page<HotelPriceDto> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Integer dateCount,
            @Param("minPrice") BigDecimal minPrice, // NEW
            @Param("maxPrice") BigDecimal maxPrice, // NEW
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}